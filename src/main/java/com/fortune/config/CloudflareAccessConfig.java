package com.fortune.config;

import java.net.URI;
import java.time.Duration;
import java.util.List;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.nimbusds.jose.jwk.JWKSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Configuration
public class CloudflareAccessConfig {
    private static final Duration JWK_CONNECT_TIMEOUT = Duration.ofSeconds(3);
    private static final Duration JWK_READ_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration JWK_CACHE_TTL = Duration.ofMinutes(5);

    @Bean
    public JwtDecoder cloudflareAccessJwtDecoder(
            @Value("${app.fortune.security.cloudflare-access.enabled:false}") boolean enabled,
            @Value("${app.fortune.security.cloudflare-access.team-domain:}") String teamDomain,
            @Value("${app.fortune.security.cloudflare-access.audience:}") String audience) {
        String normalizedDomain = normalizeTeamDomain(teamDomain);
        if (!enabled || normalizedDomain == null || audience.isBlank()) {
            return token -> {
                throw new JwtException("Cloudflare Access verification is not configured");
            };
        }

        String jwkSetUri = normalizedDomain + "/cdn-cgi/access/certs";
        RestTemplate restOperations = cloudflareJwkRestOperations();
        Cache jwkSetCache = new CaffeineCache(
                "cloudflare-access-jwks",
                Caffeine.newBuilder()
                        .expireAfterWrite(JWK_CACHE_TTL)
                        .build());
        preloadJwkSet(restOperations, jwkSetCache, jwkSetUri);

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(jwkSetUri)
                .restOperations(restOperations)
                .cache(jwkSetCache)
                .build();
        OAuth2TokenValidator<Jwt> issuerValidator =
                JwtValidators.createDefaultWithIssuer(normalizedDomain);
        OAuth2TokenValidator<Jwt> audienceValidator = jwt ->
                jwt.getAudience().contains(audience)
                        ? OAuth2TokenValidatorResult.success()
                        : OAuth2TokenValidatorResult.failure(new OAuth2Error(
                                "invalid_token",
                                "Cloudflare Access audience mismatch",
                                null));
        decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                List.of(issuerValidator, audienceValidator)));
        return decoder;
    }

    static void preloadJwkSet(
            RestOperations restOperations,
            Cache cache,
            String jwkSetUri) {
        try {
            String jwkSet = restOperations.getForObject(jwkSetUri, String.class);
            if (jwkSet == null || jwkSet.isBlank()) {
                throw new IllegalStateException("Cloudflare Access JWK response is empty");
            }
            JWKSet.parse(jwkSet);
            cache.put(jwkSetUri, jwkSet);
            log.info("Cloudflare Access JWK 사전 로딩 완료");
        } catch (Exception exception) {
            log.error("Cloudflare Access JWK 사전 로딩 실패: {}",
                    exception.getClass().getSimpleName());
            throw new IllegalStateException(
                    "Cloudflare Access JWK 사전 로딩에 실패했습니다.", exception);
        }
    }

    private RestTemplate cloudflareJwkRestOperations() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(JWK_CONNECT_TIMEOUT);
        factory.setReadTimeout(JWK_READ_TIMEOUT);
        return new RestTemplate(factory);
    }

    public static String normalizeTeamDomain(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            URI uri = URI.create(value.trim());
            String host = uri.getHost();
            if (!"https".equalsIgnoreCase(uri.getScheme())
                    || host == null
                    || !host.endsWith(".cloudflareaccess.com")
                    || uri.getUserInfo() != null
                    || uri.getPort() != -1
                    || (uri.getPath() != null && !uri.getPath().isBlank() && !"/".equals(uri.getPath()))
                    || uri.getQuery() != null
                    || uri.getFragment() != null) {
                return null;
            }
            return "https://" + host.toLowerCase();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
