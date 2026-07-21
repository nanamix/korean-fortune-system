package com.fortune.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Slf4j
@Service
public class SlackService {
    @Value("${app.fortune.slack.webhook-url:}")
    private String webhookUrl;
    private final RestTemplate restTemplate = new RestTemplate();

    public void sendMessage(String message, String requestedUrl) {
        String target = requestedUrl == null || requestedUrl.isBlank() ? webhookUrl : requestedUrl;
        if (target == null || target.isBlank() || !target.startsWith("https://hooks.slack.com/services/")) {
            throw new IllegalArgumentException("Slack webhook URL이 설정되지 않았거나 형식이 올바르지 않습니다.");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        restTemplate.postForObject(target, new HttpEntity<>(Map.of("text", message), headers), String.class);
    }
}
