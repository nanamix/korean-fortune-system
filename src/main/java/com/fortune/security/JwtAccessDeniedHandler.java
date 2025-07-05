package com.fortune.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

/**
 * 🔒 JWT 접근 거부 핸들러
 * 
 * <p>권한이 없는 요청에 대한 처리를 담당합니다.</p>
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    /**
     * 권한이 없는 요청에 대한 처리
     * 
     * @param request 요청
     * @param response 응답
     * @param accessDeniedException 접근 거부 예외
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
       
        /* 권한이 없는 요청에 대한 로그 출력 */
        log.warn("🔒 접근 권한 없음: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        /* 응답 설정 */
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        /* 응답 타입 설정 */
        response.setContentType("application/json;charset=UTF-8");
        
        /* 응답 생성 */
        String jsonResponse = String.format("""
            {
                "success": false,
                "message": "접근 권한이 없습니다",
                "error": "FORBIDDEN",
                "path": "%s",
                "timestamp": "%s"
            }
            """, request.getRequestURI(), LocalDateTime.now());
        
        /* 응답 쓰기 */
        response.getWriter().write(jsonResponse);
    }
}