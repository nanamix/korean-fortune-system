package com.fortune.config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import java.util.List;
/**
 * 🔒 Spring Security 6.x 최신 설정 적용 (Warning 없음)
 *
 * <p>운세 API를 위한 최소한의 보안 설정</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    /**
     * 🔐 보안 활성화 시 설정
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    @ConditionalOnProperty(name = "app.fortune.security.enabled", havingValue = "true")
    public SecurityFilterChain securedFilterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(
                                "/api/fortune/**",          // 모든 운세 API 허용
                                "/swagger-ui/**",           // Swagger UI
                                "/api-docs/**",             // API 문서
                                "/actuator/health",         // 헬스체크
                                "/h2-console/**",           // H2 콘솔
                                "/error"                    // 에러 페이지
                        ).permitAll()
                        .requestMatchers("/actuator/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // 최신 Lambda DSL 방식으로 헤더 설정
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin())
                        .contentTypeOptions(contentTypeOptions -> {})
                        .httpStrictTransportSecurity(hstsConfig -> {})
                )
                .build();
    }
    /**
     * 🚫 보안 비활성화 시 설정 (기본)
     * 
     * @param http HttpSecurity
     * @return SecurityFilterChain
     * @throws Exception
     */
    @Bean
    @ConditionalOnProperty(name = "app.fortune.security.enabled", havingValue = "false", matchIfMissing = true)
    public SecurityFilterChain openFilterChain(HttpSecurity http) throws Exception {
        // CSRF 비활성화
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                // H2 콘솔을 위한 프레임 옵션 비활성화
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.disable())
                )
                .build();
    }
    /**
     * 🌐 CORS 설정
     * 
     * @return CorsConfigurationSource
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        // CORS 설정
        CorsConfiguration configuration = new CorsConfiguration();
        // 개발 환경을 위한 허용 설정
        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:*",
                "https://localhost:*",
                "http://127.0.0.1:*",
                "https://127.0.0.1:*"
        ));
        configuration.setAllowedMethods(List.of(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        // 허용 헤더 설정
        configuration.setAllowedHeaders(List.of("*"));
        // 자격 증명 허용
        configuration.setAllowCredentials(true);
        // 최대 나이 설정
        configuration.setMaxAge(3600L);
        // CORS 설정 소스 생성
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
    /**
     * 🔑 비밀번호 인코더
     * 
     * @return PasswordEncoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCryptPasswordEncoder 생성
        return new BCryptPasswordEncoder();
    }
}
