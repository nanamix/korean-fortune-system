package com.fortune.config;

import com.fortune.security.JwtTokenUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "app.fortune.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @MockitoBean
    private JwtTokenUtil jwtTokenUtil;

    @MockitoBean
    private JavaMailSender javaMailSender;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void permitsPublicWebAssetsAndRequiredApis() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk());
        mockMvc.perform(get("/index.html")).andExpect(status().isOk());
        mockMvc.perform(get("/fortune-app.html")).andExpect(status().isOk());
        mockMvc.perform(get("/manifest.json")).andExpect(status().isOk());
        mockMvc.perform(get("/sw.js")).andExpect(status().isOk());
        mockMvc.perform(get("/api/system/status")).andExpect(status().isOk());
        mockMvc.perform(get("/api/docs")).andExpect(status().isOk());
    }

    @Test
    void keepsNonHealthActuatorEndpointsProtected() throws Exception {
        mockMvc.perform(get("/actuator/info")).andExpect(status().isForbidden());
    }
}
