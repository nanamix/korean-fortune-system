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
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /**
     * JWT 토큰 유틸리티
     * - Autowired 어노테이션을 사용하여 JWT 토큰 유틸리티를 주입합니다.
     * - JwtTokenUtil 클래스를 사용하여 JWT 토큰을 검증합니다.
     * - UserDetailsService 인터페이스를 사용하여 사용자 정보를 조회합니다.
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    /**
     * 사용자 정보 서비스
     * - Autowired 어노테이션을 사용하여 사용자 정보 서비스를 주입합니다.
     * - UserDetailsService 인터페이스를 사용하여 사용자 정보를 조회합니다.
     * 
     * @author 하진영
     * @version 2.5.0
     * @since 2025-06-24
     */
    @Autowired
    private UserDetailsService userDetailsService;

    /**
     * 필터 내부 처리
     * 
     * @param request 요청
     * @param response 응답
     * @param filterChain 필터 체인
     */
    @SuppressWarnings("null")
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        /* JWT 토큰 추출 */
        String jwt = getJwtFromRequest(request);
        
        /* JWT 토큰이 있는 경우 */
        if (StringUtils.hasText(jwt)) {
            try {
                String username = jwtTokenUtil.getUsernameFromToken(jwt);

                /* 사용자 정보가 있고 인증 정보가 없는 경우 */
                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    /* JWT 토큰 검증 */
                    if (jwtTokenUtil.validateToken(jwt, userDetails)) {
                        /* 토큰에서 권한 정보 추출 */
                        Collection<String> authorities = jwtTokenUtil.getAuthoritiesFromToken(jwt);
                        Collection<SimpleGrantedAuthority> grantedAuthorities = authorities.stream()
                                .map(SimpleGrantedAuthority::new)
                                .collect(Collectors.toList());

                        /* 인증 정보 설정 */
                        UsernamePasswordAuthenticationToken authentication = 
                                new UsernamePasswordAuthenticationToken(userDetails, null, grantedAuthorities);
                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        /* 인증 정보 설정 */
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        log.debug("✅ JWT 인증 성공: {}", username);
                    }
                }
            } catch (Exception e) {
                /* 예외 처리 */
                log.error("❌ JWT 인증 처리 중 오류: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        /* 필터 체인 실행 */
        filterChain.doFilter(request, response);
    }

    /**
     * 요청에서 JWT 토큰 추출
     * 
     * @param request 요청
     * @return JWT 토큰
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        /* Authorization 헤더에서 JWT 토큰 추출 */
        String bearerToken = request.getHeader("Authorization");

        /* Bearer 토큰 형식인 경우 토큰 추출 */
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 특정 요청 경로에 대해 필터를 적용하지 않을 수 있습니다 (선택사항)
     * 
     * @param request 요청
     * @return 필터 적용 여부
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        /* 요청 경로 추출 */
        String path = request.getRequestURI();
        /* 로그인, 회원가입 등의 공개 엔드포인트는 JWT 검증 제외 */
        return path.startsWith("/api/auth/") || 
               path.startsWith("/api/public/") ||
               path.equals("/health") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs");
    }
}