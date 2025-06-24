package com.fortune.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * 🔑 JWT 인증 필터 클래스
 * 
 * <p>모든 HTTP 요청에서 JWT 토큰을 검증하고 인증 정보를 설정합니다.</p>
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String jwt = getJwtFromRequest(request);
        
        if (StringUtils.hasText(jwt)) {
            try {
                String username = jwtTokenUtil.getUsernameFromToken(jwt);
                
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    
                    if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                        // 토큰에서 권한 정보 추출
                        Collection<String> authorities = jwtTokenUtil.getAuthoritiesFromToken(jwt);
                        Collection<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, grantedAuthorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("✅ JWT 인증 성공: {}", username);
                    }
                }
            } catch (Exception e) {
                log.error("❌ JWT 인증 처리 중 오류: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰 추출
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 특정 요청 경로에 대해 필터를 적용하지 않을 수 있습니다 (선택사항)
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        // 로그인, 회원가입 등의 공개 엔드포인트는 JWT 검증 제외
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/public/") ||
               path.equals("/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs");
    }
}