package com.fortune.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * 웹 관련 설정
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-01-05
 */
@Configuration
public class WebConfig {

    /**
     * RestTemplate 빈 설정
     * 텔레그램 API 호출을 위해 사용
     * 
     * @return RestTemplate 인스턴스
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
} 