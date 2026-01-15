package com.example.aiexcel.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.FileSystemResource;
import java.io.*;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EnvFileEnvironmentPostProcessor implements EnvironmentPostProcessor {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        // 获取当前工作目录
        String currentDir = System.getProperty("user.dir");
        String envFilePath = currentDir + File.separator + ".env";

        // 手动解析 .env 文件
        Properties properties = loadEnvFile(envFilePath);
        if (properties != null) {
            // 将属性添加到环境中
            // 将 .env 中的常见 Spring 配置项映射为 Spring 能识别的属性
            String serverPort = properties.getProperty("SERVER_PORT");
            if (serverPort != null && !serverPort.isEmpty()) {
                properties.setProperty("server.port", serverPort);
                // 也设置为系统属性，以防其他组件读取 System.getProperty
                System.setProperty("server.port", serverPort);
            }

            String activeProfile = properties.getProperty("SPRING_PROFILES_ACTIVE");
            if (activeProfile != null && !activeProfile.isEmpty()) {
                properties.setProperty("spring.profiles.active", activeProfile);
                System.setProperty("spring.profiles.active", activeProfile);
            }

            // 映射 QWEN_* 环境变量到应用使用的属性键
            String qwenApiKey = properties.getProperty("QWEN_API_KEY");
            if (qwenApiKey != null && !qwenApiKey.isEmpty()) {
                properties.setProperty("qwen.api.api-key", qwenApiKey);
                // 不在日志中打印敏感的 API Key
                System.setProperty("qwen.api.api-key", qwenApiKey);
            }

            String qwenModel = properties.getProperty("QWEN_MODEL_NAME");
            if (qwenModel != null && !qwenModel.isEmpty()) {
                // 进一步清理：去掉行内注释并修剪
                String sanitizedModel = qwenModel;
                int hashIdx = sanitizedModel.indexOf('#');
                if (hashIdx >= 0) {
                    sanitizedModel = sanitizedModel.substring(0, hashIdx).trim();
                }
                int slashIdx = sanitizedModel.indexOf("//");
                if (slashIdx >= 0) {
                    sanitizedModel = sanitizedModel.substring(0, slashIdx).trim();
                }
                properties.setProperty("QWEN_MODEL_NAME", sanitizedModel);
                properties.setProperty("qwen.api.default-model", sanitizedModel);
                System.setProperty("qwen.api.default-model", sanitizedModel);
            }

            String qwenBase = properties.getProperty("QWEN_API_BASE_URL");
            if (qwenBase != null && !qwenBase.isEmpty()) {
                properties.setProperty("qwen.api.base-url", qwenBase);
                System.setProperty("qwen.api.base-url", qwenBase);
            }

            environment.getPropertySources().addFirst(new org.springframework.core.env.PropertiesPropertySource("env", properties));

            // 输出调试信息
            System.out.println("Successfully loaded .env file from: " + envFilePath);

            // 输出关键配置值（不包含敏感信息）
            String model = properties.getProperty("QWEN_MODEL_NAME");
            if (model != null) {
                System.out.println("QWEN_MODEL_NAME loaded: " + model);
            }
        } else {
            System.err.println("Could not find or load .env file at: " + envFilePath);
        }
    }

    private Properties loadEnvFile(String envFilePath) {
        Properties properties = new Properties();
        FileSystemResource resource = new FileSystemResource(envFilePath);

        if (!resource.exists()) {
            return null;
        }

        Pattern pattern = Pattern.compile("^([A-Za-z_][A-Za-z0-9_]*)=(.*)$");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // 忽略空行和注释行
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }

                // 解析键值对
                Matcher matcher = pattern.matcher(line);
                if (matcher.matches()) {
                    String key = matcher.group(1);
                    String value = matcher.group(2).trim();

                    // 如果值被双引号或单引号包裹，去除包裹引号
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                        (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    } else {
                        // 去除未被引号包裹情况下的行内注释（例如: value  # comment）
                        int commentIdx = value.indexOf('#');
                        if (commentIdx >= 0) {
                            value = value.substring(0, commentIdx).trim();
                        }
                        // 也处理常见的双斜线注释
                        int slashIdx = value.indexOf("//");
                        if (slashIdx >= 0) {
                            value = value.substring(0, slashIdx).trim();
                        }
                    }

                    properties.setProperty(key, value);
                }
            }
        } catch (IOException e) {
            System.err.println("Could not load .env file: " + e.getMessage());
            return null;
        }

        return properties;
    }
}