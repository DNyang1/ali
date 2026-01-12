package com.finalProject.ali.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("ALI Project API Document")
                        .description("B2B 무역 플랫폼 API 명세서입니다.")
                        .version("v1.0.0"));
    }
}