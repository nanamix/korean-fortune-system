package com.fortune.security;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

import com.nimbusds.jose.RemoteKeySourceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "app.fortune.security.enabled=true",
                "app.fortune.security.cloudflare-access.enabled=true",
                "app.fortune.security.cloudflare-access.team-domain=https://nanamix.cloudflareaccess.com",
                "app.fortune.security.cloudflare-access.audience=test-audience"
        })
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CloudflareAccessFilterTest {

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsPublicPageWithoutAccessAssertion() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("CF_ACCESS_REQUIRED"));
    }

    @Test
    void permitsPublicPageWithValidAccessAssertion() throws Exception {
        when(jwtDecoder.decode("valid-assertion")).thenReturn(validJwt());

        mockMvc.perform(get("/").header("Cf-Access-Jwt-Assertion", "valid-assertion"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsInvalidAccessAssertion() throws Exception {
        when(jwtDecoder.decode("invalid-assertion")).thenThrow(new JwtException("invalid"));

        mockMvc.perform(get("/").header("Cf-Access-Jwt-Assertion", "invalid-assertion"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("CF_ACCESS_INVALID"));

        verify(jwtDecoder).decode("invalid-assertion");
    }

    @Test
    void retriesTransientJwkFailureOnce() throws Exception {
        when(jwtDecoder.decode("transient-assertion"))
                .thenThrow(transientJwkFailure())
                .thenReturn(validJwt());

        mockMvc.perform(get("/").header("Cf-Access-Jwt-Assertion", "transient-assertion"))
                .andExpect(status().isOk());

        verify(jwtDecoder, times(2)).decode("transient-assertion");
    }

    @Test
    void returnsServiceUnavailableWhenJwkRetrievalKeepsFailing() throws Exception {
        when(jwtDecoder.decode("transient-assertion")).thenThrow(transientJwkFailure());

        mockMvc.perform(get("/").header("Cf-Access-Jwt-Assertion", "transient-assertion"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.error.code").value("CF_ACCESS_VERIFIER_UNAVAILABLE"));

        verify(jwtDecoder, times(2)).decode("transient-assertion");
    }

    @Test
    void keepsLivenessHealthCheckAvailableWithoutAccessAssertion() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void authenticatesBrowserHealthCheckWhenAccessAssertionIsPresent() throws Exception {
        when(jwtDecoder.decode("valid-assertion")).thenReturn(validJwt());

        mockMvc.perform(get("/actuator/health")
                        .header("Cf-Access-Jwt-Assertion", "valid-assertion"))
                .andExpect(status().isOk());

        verify(jwtDecoder).decode("valid-assertion");
    }

    @Test
    void doesNotGrantAdminRoleToAccessUser() throws Exception {
        when(jwtDecoder.decode("valid-assertion")).thenReturn(validJwt());

        mockMvc.perform(get("/actuator/info")
                        .header("Cf-Access-Jwt-Assertion", "valid-assertion"))
                .andExpect(status().isForbidden());
    }

    @Test
    void failsClosedWhenAccessConfigurationIsMissing() throws Exception {
        JwtDecoder decoder = mock(JwtDecoder.class);
        CloudflareAccessFilter filter = new CloudflareAccessFilter(
                true,
                "",
                "",
                decoder);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertThat(response.getStatus()).isEqualTo(503);
        assertThat(response.getContentAsString()).contains("CF_ACCESS_NOT_CONFIGURED");
        verifyNoInteractions(decoder);
    }

    private Jwt validJwt() {
        Instant now = Instant.now();
        return Jwt.withTokenValue("valid-assertion")
                .header("alg", "RS256")
                .subject("user@example.com")
                .audience(List.of("test-audience"))
                .issuedAt(now.minusSeconds(10))
                .expiresAt(now.plusSeconds(300))
                .build();
    }

    private JwtException transientJwkFailure() {
        return new JwtException(
                "Unable to obtain the keys",
                new RemoteKeySourceException(
                        "Couldn't retrieve remote JWK set",
                        new IOException("temporary network failure")));
    }
}
