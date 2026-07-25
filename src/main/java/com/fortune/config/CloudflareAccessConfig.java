package com.fortune.config;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

@Configuration
public class CloudflareAccessConfig {

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

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(normalizedDomain + "/cdn-cgi/access/certs")
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
