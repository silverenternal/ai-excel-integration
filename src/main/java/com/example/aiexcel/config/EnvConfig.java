package com.example.aiexcel.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;


/**
 * 配置类用于加载.env文件
 * 注意：由于使用了EnvFileEnvironmentPostProcessor，此配置类可能冗余
 * 保持此文件作为备用加载方式
 */
@Configuration
public class EnvConfig {

    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();

        // 加载 .env 文件
        Resource resource = new FileSystemResource(".env");
        if (resource.exists()) {
            configurer.setLocation(resource);
        }

        configurer.setIgnoreResourceNotFound(true);
        configurer.setIgnoreUnresolvablePlaceholders(true);

        return configurer;
    }
}