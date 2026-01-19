package com.example.aiexcel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        String allowed = System.getenv("API_ALLOW_ORIGIN");
        if (allowed == null || allowed.isEmpty()) {
            // 默认允许常用本地端口，或使用通配符
            allowed = "http://localhost:8080,http://localhost:8081";
        }

        String[] origins = allowed.split("\s*,\s*");

        registry.addMapping("/api/**")
                .allowedOrigins(origins)
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(false)
                .maxAge(3600);
    }
}
