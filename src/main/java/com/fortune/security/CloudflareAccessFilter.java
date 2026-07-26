package com.fortune.security;

import java.io.IOException;
import java.util.List;

import com.fortune.config.CloudflareAccessConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class CloudflareAccessFilter extends OncePerRequestFilter {
    private static final String ASSERTION_HEADER = "Cf-Access-Jwt-Assertion";

    private final boolean enabled;
    private final boolean configured;
    private final JwtDecoder jwtDecoder;

    public CloudflareAccessFilter(
            @Value("${app.fortune.security.cloudflare-access.enabled:false}") boolean enabled,
            @Value("${app.fortune.security.cloudflare-access.team-domain:}") String teamDomain,
            @Value("${app.fortune.security.cloudflare-access.audience:}") String audience,
            JwtDecoder jwtDecoder) {
        this.enabled = enabled;
        this.configured = CloudflareAccessConfig.normalizeTeamDomain(teamDomain) != null
                && audience != null
                && !audience.isBlank();
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !enabled
                || "OPTIONS".equalsIgnoreCase(request.getMethod())
                || isInternalHealthCheck(request);
    }

    private boolean isInternalHealthCheck(HttpServletRequest request) {
        if (!"/actuator/health".equals(request.getRequestURI())) {
            return false;
        }
        String assertion = request.getHeader(ASSERTION_HEADER);
        return assertion == null || assertion.isBlank();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        if (!configured) {
            writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "CF_ACCESS_NOT_CONFIGURED", "접근 인증 설정이 완료되지 않았습니다.");
            return;
        }

        String assertion = request.getHeader(ASSERTION_HEADER);
        if (assertion == null || assertion.isBlank()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED,
                    "CF_ACCESS_REQUIRED", "Cloudflare Access 인증이 필요합니다.");
            return;
        }

        try {
            Jwt jwt = jwtDecoder.decode(assertion);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(jwt.getSubject(), jwt, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException exception) {
            SecurityContextHolder.clearContext();
            writeError(response, HttpServletResponse.SC_FORBIDDEN,
                    "CF_ACCESS_INVALID", "유효하지 않은 접근 인증입니다.");
        }
    }

    private void writeError(
            HttpServletResponse response,
            int status,
            String code,
            String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().printf(
                "{\"success\":false,\"error\":{\"code\":\"%s\",\"message\":\"%s\"}}",
                code,
                message);
    }
}
