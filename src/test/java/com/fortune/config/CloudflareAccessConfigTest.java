package com.fortune.config;

import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestOperations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CloudflareAccessConfigTest {
    private static final String JWK_SET_URI =
            "https://nanamix.cloudflareaccess.com/cdn-cgi/access/certs";

    @Test
    void preloadsValidJwkSetIntoDecoderCache() {
        RestOperations restOperations = mock(RestOperations.class);
        ConcurrentMapCache cache = new ConcurrentMapCache("cloudflare-access-jwks");
        String jwkSet = "{\"keys\":[]}";
        when(restOperations.getForObject(JWK_SET_URI, String.class)).thenReturn(jwkSet);

        CloudflareAccessConfig.preloadJwkSet(restOperations, cache, JWK_SET_URI);

        assertThat(cache.get(JWK_SET_URI, String.class)).isEqualTo(jwkSet);
    }

    @Test
    void failsStartupWhenJwkSetCannotBeRetrieved() {
        RestOperations restOperations = mock(RestOperations.class);
        ConcurrentMapCache cache = new ConcurrentMapCache("cloudflare-access-jwks");
        when(restOperations.getForObject(JWK_SET_URI, String.class))
                .thenThrow(new ResourceAccessException("connection failed"));

        assertThatThrownBy(() ->
                CloudflareAccessConfig.preloadJwkSet(restOperations, cache, JWK_SET_URI))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cloudflare Access JWK");
    }

    @Test
    void failsStartupWhenJwkSetResponseIsMalformed() {
        RestOperations restOperations = mock(RestOperations.class);
        ConcurrentMapCache cache = new ConcurrentMapCache("cloudflare-access-jwks");
        when(restOperations.getForObject(JWK_SET_URI, String.class))
                .thenReturn("{\"keys\":");

        assertThatThrownBy(() ->
                CloudflareAccessConfig.preloadJwkSet(restOperations, cache, JWK_SET_URI))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cloudflare Access JWK");
    }
}
