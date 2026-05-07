package com.fortune.security;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
/**
 * 🚫 JWT 인증 진입점 클래스
 * 
 * <p>인증되지 않은 사용자의 접근 시 처리를 담당합니다.</p>
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    /**
     * 객체 매퍼
     */
    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * 인증되지 않은 접근 시도 처리
     */
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {
        /* 인증되지 않은 접근 시도 로깅 */
        log.error("❌ 인증되지 않은 접근 시도: {} {}", request.getMethod(), request.getRequestURI());
        /* 인증 오류 로깅 */
        log.error("❌ 인증 오류: {}", authException.getMessage());
        /* 응답 설정 */
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        /* 응답 상태 설정 */
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        /* 응답 인코딩 설정 */
        response.setCharacterEncoding("UTF-8");
        /* 응답 생성 */
        Map<String, Object> errorResponse = new HashMap<>();
        errorResponse.put("timestamp", LocalDateTime.now().toString());
        errorResponse.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        errorResponse.put("error", "Unauthorized");
        errorResponse.put("message", "인증이 필요합니다. 유효한 JWT 토큰을 제공해주세요.");
        errorResponse.put("path", request.getRequestURI());
        /* 응답 쓰기 */
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}
