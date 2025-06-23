package com.fortune.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("🔮 한국형 만세력 운세 시스템 API")
                        .description("전통 사주팔자와 토정비결을 활용한 한국형 운세 시스템입니다.")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Korean Fortune System")
                                .email("support@fortune.com")
                                .url("https://github.com/korean-fortune-system"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")));
    }
}
