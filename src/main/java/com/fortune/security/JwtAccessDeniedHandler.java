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
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                      AccessDeniedException accessDeniedException) throws IOException, ServletException {
        
        log.warn("🔒 접근 권한 없음: {} - {}", request.getRequestURI(), accessDeniedException.getMessage());
        
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType("application/json;charset=UTF-8");
        
        String jsonResponse = String.format("""
            {
                "success": false,
                "message": "접근 권한이 없습니다",
                "error": "FORBIDDEN",
                "path": "%s",
                "timestamp": "%s"
            }
            """, request.getRequestURI(), LocalDateTime.now());
        
        response.getWriter().write(jsonResponse);
    }
}