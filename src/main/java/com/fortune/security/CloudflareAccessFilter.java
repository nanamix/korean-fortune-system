package com.fortune.security;

import java.io.IOException;
import java.util.List;

import com.fortune.config.CloudflareAccessConfig;
import com.nimbusds.jose.RemoteKeySourceException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
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
            Jwt jwt = decodeWithTransientRetry(assertion, request.getRequestURI());
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(jwt.getSubject(), jwt, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            filterChain.doFilter(request, response);
        } catch (JwtException exception) {
            SecurityContextHolder.clearContext();
            if (isTransientVerifierFailure(exception)) {
                log.error("Cloudflare Access 검증 서비스 일시 실패: path={}, cause={}",
                        request.getRequestURI(), failureType(exception));
                writeError(response, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "CF_ACCESS_VERIFIER_UNAVAILABLE",
                        "접근 인증 검증 서비스를 일시적으로 사용할 수 없습니다.");
            } else {
                log.warn("Cloudflare Access 인증 거부: path={}, cause={}",
                        request.getRequestURI(), failureType(exception));
                writeError(response, HttpServletResponse.SC_FORBIDDEN,
                        "CF_ACCESS_INVALID", "유효하지 않은 접근 인증입니다.");
            }
        }
    }

    private Jwt decodeWithTransientRetry(String assertion, String requestUri) {
        try {
            return jwtDecoder.decode(assertion);
        } catch (JwtException exception) {
            if (!isTransientVerifierFailure(exception)) {
                throw exception;
            }
            log.warn("Cloudflare Access JWK 조회 일시 실패로 1회 재시도: path={}, cause={}",
                    requestUri, failureType(exception));
            return jwtDecoder.decode(assertion);
        }
    }

    private boolean isTransientVerifierFailure(Throwable exception) {
        Throwable current = exception;
        while (current != null) {
            if (current instanceof RemoteKeySourceException
                    || current instanceof ResourceAccessException
                    || current instanceof IOException) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String failureType(Throwable exception) {
        Throwable current = exception;
        Throwable last = exception;
        while (current != null) {
            last = current;
            current = current.getCause();
        }
        return last.getClass().getSimpleName();
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
