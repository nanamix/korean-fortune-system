package com.fortune.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/*
 * 보안 설정
 * 보안 필터 체인 설정
 * CORS 설정
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * 보안 필터 체인 설정
     * CORS 설정
     * CSRF 설정
     * 인증 요청 설정
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // CORS 설정
                .csrf(csrf -> csrf.disable())                                    // CSRF 설정
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/api/fortune/**").permitAll()           // 허용 URL
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()                            // 인증 요청 설정
                );

        return http.build();
    }

    /*
     * CORS 설정
     * CORS 설정 소스 반환
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));            // 모든 출처 허용
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));                    // 모든 헤더 허용
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource(); // URL 기반 CORS 설정 소스 생성
        source.registerCorsConfiguration("/api/**", configuration);                    // CORS 설정 등록
        return source;                                                                // CORS 설정 소스 반환
    }
}
