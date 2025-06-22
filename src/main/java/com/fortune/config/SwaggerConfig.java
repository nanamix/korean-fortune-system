package com.fortune.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("한국형 만세력 운세 시스템 API")
                        .description("전통 사주학 기반의 종합 운세 서비스 API")
                        .version("v1.0.0")
                        .contact(new Contact()
                                .name("Korean Fortune Team")
                                .email("support@korean-fortune.com")
                                .url("https://korean-fortune.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("개발 서버"),
                        new Server()
                                .url("https://api.korean-fortune.com")
                                .description("운영 서버")))
                .tags(List.of(
                        new Tag()
                                .name("사주팔자")
                                .description("사주팔자 계산 관련 API"),
                        new Tag()
                                .name("일일운세")
                                .description("오늘/내일/지정일 운세 API"),
                        new Tag()
                                .name("토정비결")
                                .description("토정비결 64괘 연간 운세 API"),
                        new Tag()
                                .name("간지달력")
                                .description("간지달력 및 절기 정보 API"),
                        new Tag()
                                .name("별자리운세")
                                .description("서양 별자리 운세 API"),
                        new Tag()
                                .name("시스템")
                                .description("시스템 상태 및 헬스체크 API")));
    }
}
