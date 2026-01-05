package com.finalProject.ali.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 1. 브라우저가 사용하는 주소 패턴: http://localhost/upload/**
        // 2. 실제 파일이 저장된 내 컴퓨터 경로: file:///C:/upload/ali_uploads/

        registry.addResourceHandler("/upload/**")
                .addResourceLocations("file:///D:/upload/ali_uploads/");
    }
}