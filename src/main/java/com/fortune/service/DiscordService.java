package com.fortune.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;
import java.util.Set;

/**
 * Discord 알림 서비스 — Incoming Webhook 으로 운세 결과를 채널에 전송한다.
 *
 * <p>서버 기본 webhook(app.fortune.discord.webhook-url) 또는 요청별 webhook 을 사용한다.
 * 요청별 URL 은 SSRF 방지를 위해 Discord 공식 호스트로 제한한다.</p>
 */
@Slf4j
@Service
public class DiscordService {

    /** SSRF 방지: 허용 webhook 호스트. */
    private static final Set<String> ALLOWED_HOSTS = Set.of(
            "discord.com", "discordapp.com", "canary.discord.com", "ptb.discord.com");
    /** Discord 메시지 content 길이 제한. */
    private static final int MAX_CONTENT = 2000;

    @Value("${app.fortune.discord.webhook-url:}")
    private String webhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public DiscordService() {
        log.info("📢 DiscordService 초기화");
    }

    /** 서버 기본 webhook 으로 전송. */
    public void sendMessage(String message) {
        sendMessage(message, null);
    }

    /**
     * 지정 webhook 으로 전송. {@code url} 이 비어 있으면 서버 기본값을 사용한다.
     * 실패는 로깅만 하고 예외를 던지지 않는다(알림은 부가 기능).
     */
    public void sendMessage(String message, String url) {
        String target = (url == null || url.isBlank()) ? webhookUrl : url;
        if (target == null || target.isBlank()) {
            log.warn("⚠️ Discord webhook URL 미설정 — 전송 건너뜀");
            return;
        }
        if (!isAllowedWebhook(target)) {
            log.warn("⚠️ 허용되지 않은 Discord webhook URL — 전송 차단");
            return;
        }
        try {
            String content = message.length() > MAX_CONTENT
                    ? message.substring(0, MAX_CONTENT - 3) + "..." : message;
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("content", content), headers);
            restTemplate.postForObject(target, entity, String.class);
            log.info("📢 Discord 메시지 전송 완료");
        } catch (Exception e) {
            log.error("❌ Discord 메시지 전송 실패", e);
        }
    }

    /** Discord 공식 webhook(https, 허용 호스트, /api/webhooks/ 경로)만 허용. */
    boolean isAllowedWebhook(String url) {
        try {
            URI uri = URI.create(url);
            return "https".equalsIgnoreCase(uri.getScheme())
                    && uri.getHost() != null
                    && ALLOWED_HOSTS.contains(uri.getHost().toLowerCase())
                    && uri.getPath() != null
                    && uri.getPath().startsWith("/api/webhooks/");
        } catch (Exception e) {
            return false;
        }
    }
}
