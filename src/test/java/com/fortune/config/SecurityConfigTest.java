package com.fortune.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fortune.security.JwtTokenUtil;
import com.fortune.service.LocationSearchService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.fortune.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @MockitoBean
    private LocationSearchService locationSearchService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsPublicWebAssetsAndRequiredApis() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/fortune-app.html")).andExpect(status().isOk());
        mockMvc.perform(get("/manifest.json")).andExpect(status().isOk());
        mockMvc.perform(get("/sw.js")).andExpect(status().isOk());
        mockMvc.perform(get("/api/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.version").isNotEmpty())
                .andExpect(jsonPath("$.data.currentTime").isNotEmpty())
                .andExpect(jsonPath("$.data.uptime").isNotEmpty())
                .andExpect(jsonPath("$.data.features").isMap());
        mockMvc.perform(get("/api/docs")).andExpect(status().isOk());
        when(locationSearchService.search("서울")).thenReturn(List.of());
        mockMvc.perform(get("/api/location/search").param("q", "서울"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void keepsNonHealthActuatorEndpointsProtected() throws Exception {
        mockMvc.perform(get("/actuator/info")).andExpect(status().isForbidden());
        mockMvc.perform(get("/actuator/aiNarrationReceipts"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get("/actuator/aiNarrationCanary"))
                .andExpect(status().isForbidden());
    }

    @Test
    void sendsCloudflareAccessCookieWithApiRequests() throws IOException {
        String html = new ClassPathResource("static/fortune-app.html")
                .getContentAsString(StandardCharsets.UTF_8);

        assertThat(html.split("credentials: 'same-origin'", -1)).hasSize(3);
    }

    @Test
    void permitsProductionOriginForFortuneApiPosts() throws Exception {
        mockMvc.perform(options("/api/fortune/zodiac")
                        .header("Origin", "https://saju.jyha.net")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "https://saju.jyha.net"));
    }
}
