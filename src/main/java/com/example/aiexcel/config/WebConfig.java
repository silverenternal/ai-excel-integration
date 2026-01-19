package com.example.aiexcel.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 确保静态资源可以从 classpath:/static/ 提供
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600); // 设置缓存时间
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // 只将根路径转发到 index.html
        // 这样 SPA 路由才能正常工作，同时不影响 API 路径
        registry.addViewController("/")
                .setViewName("forward:/index.html");
    }
}