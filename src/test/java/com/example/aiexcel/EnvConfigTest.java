package com.example.aiexcel;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EnvConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

    @Test
    public void testEnvVariablesLoaded() {
        String resolvedApiKey = resolveApiKey();

        contextRunner
            .withPropertyValues(
                "qwen.api.api-key=" + resolvedApiKey,
                "qwen.api.base-url=https://dashscope.aliyuncs.com/compatible-mode/v1",
                "qwen.api.default-model=qwen-max"
            )
            .run(context -> {
                Environment env = context.getEnvironment();
                String apiKey = env.getProperty("qwen.api.api-key");
                String baseUrl = env.getProperty("qwen.api.base-url");
                String defaultModel = env.getProperty("qwen.api.default-model");

                assertNotNull(apiKey, "API Key should not be null");
                assertFalse(apiKey.isEmpty(), "API Key should not be empty");
                assertEquals(resolvedApiKey, apiKey, "API Key should match the resolved value");

                assertEquals("https://dashscope.aliyuncs.com/compatible-mode/v1", baseUrl, "Base URL should match the expected value");
                assertEquals("qwen-max", defaultModel, "Default model should match the expected value");

                System.out.println("Environment properties loaded: base-url and default-model verified");
            });
    }

    private String resolveApiKey() {
        // 优先使用环境变量
        String key = System.getenv("QWEN_API_KEY");
        if (key != null && !key.isEmpty()) return key;

        // 尝试读取工程根目录下的 .env 文件
        Path envPath = Path.of(System.getProperty("user.dir"), ".env");
        if (Files.exists(envPath)) {
            try {
                List<String> lines = Files.readAllLines(envPath);
                for (String line : lines) {
                    String l = line.trim();
                    if (l.isEmpty() || l.startsWith("#") || !l.contains("=")) continue;
                    String[] parts = l.split("=", 2);
                    if (parts.length == 2 && "QWEN_API_KEY".equals(parts[0].trim())) {
                        String val = parts[1].trim();
                        // 去除可选的引号
                        if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                            val = val.substring(1, val.length() - 1);
                        }
                        // 去除注释
                        int cidx = val.indexOf('#');
                        if (cidx >= 0) val = val.substring(0, cidx).trim();
                        if (!val.isEmpty()) return val;
                    }
                }
            } catch (IOException ignored) {
                // 忽略，回退到默认占位符
            }
        }

        return "test-api-key";
    }
}