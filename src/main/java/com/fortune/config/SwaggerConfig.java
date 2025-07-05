package com.fortune.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

/**
 * Swagger 설정
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Configuration
public class SwaggerConfig {

    /**
     * Swagger 설정
     * 
     * @return OpenAPI
     */
    @Bean
    public OpenAPI customOpenAPI() {
        // OpenAPI 설정
        return new OpenAPI()
                .info(new Info()
                        .title("🔮 한국형 만세력 운세 시스템 API")
                        .description("전통 사주팔자와 토정비결을 활용한 한국형 운세 시스템입니다.")
                        .version("v2.5.0")
                        .contact(new Contact()
                                .name("Korean Fortune System")
                                .email("amdi@jyha.net")
                                .url("https://github.com/korean-fortune-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
