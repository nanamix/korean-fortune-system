package com.fortune.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
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
    private static final String NAMED_WEBHOOK_PREFIX = "DISCORD_WEBHOOK_URL_";

    private final String webhookUrl;
    private final Map<String, String> namedWebhooks;
    private final RestTemplate restTemplate;

    public DiscordService() {
        this("", new StandardEnvironment());
    }

    @Autowired
    public DiscordService(
            @Value("${app.fortune.discord.webhook-url:}") String webhookUrl,
            ConfigurableEnvironment environment) {
        this.webhookUrl = webhookUrl == null ? "" : webhookUrl.trim();
        this.namedWebhooks = loadNamedWebhooks(environment);
        // 외부 webhook 호출 — connect/read 타임아웃 지정(무제한 블로킹 방지).
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));
        this.restTemplate = new RestTemplate(factory);
        log.info("📢 DiscordService 초기화: defaultConfigured={}, namedTargets={}",
                !this.webhookUrl.isBlank(), this.namedWebhooks.keySet());
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
        sendMessage(message, url, null);
    }

    /**
     * 일회성 URL, OpenBao 관리 대상, 서버 기본값 순으로 webhook 을 선택해 전송한다.
     */
    public void sendMessage(String message, String url, String targetName) {
        if (message == null || message.isBlank()) {
            log.warn("⚠️ Discord 메시지가 비어 있음 — 전송 건너뜀");
            return;
        }
        String target = resolveWebhook(url, targetName);
        if (target == null || target.isBlank()) {
            log.warn("⚠️ Discord webhook 대상 미설정 — 전송 건너뜀: target={}",
                    safeTargetName(targetName));
            return;
        }
        if (!isAllowedWebhook(target)) {
            log.warn("⚠️ 허용되지 않은 Discord webhook URL — 전송 차단");
            return;
        }
        try {
            postMessage(message, target);
        } catch (Exception e) {
            log.error("❌ Discord 메시지 전송 실패", e);
        }
    }

    /**
     * 설정과 실제 webhook 호출을 검증하는 테스트 전송. 실패 시 호출자에게 예외를 전달한다.
     */
    public void sendTestMessage(String message, String url) {
        sendTestMessage(message, url, null);
    }

    public void sendTestMessage(String message, String url, String targetName) {
        if (message == null || message.isBlank()) {
            throw new IllegalArgumentException("Discord 테스트 메시지는 필수입니다.");
        }
        String target = resolveWebhook(url, targetName);
        if (target == null || target.isBlank()) {
            throw new IllegalStateException("Discord webhook 대상이 설정되지 않았습니다.");
        }
        if (!isAllowedWebhook(target)) {
            throw new IllegalArgumentException("Discord 공식 Webhook URL만 사용할 수 있습니다.");
        }
        postMessage(message, target);
    }

    public boolean isDefaultWebhookConfigured() {
        return !webhookUrl.isBlank() && isAllowedWebhook(webhookUrl);
    }

    public List<String> getConfiguredTargetNames() {
        return namedWebhooks.keySet().stream().sorted().toList();
    }

    private String resolveWebhook(String url, String targetName) {
        if (url != null && !url.isBlank()) {
            return url.trim();
        }
        String normalizedTarget = safeTargetName(targetName);
        if (normalizedTarget == null || "default".equals(normalizedTarget)) {
            return webhookUrl;
        }
        return namedWebhooks.get(normalizedTarget);
    }

    private Map<String, String> loadNamedWebhooks(ConfigurableEnvironment environment) {
        Map<String, String> configured = new LinkedHashMap<>();
        for (PropertySource<?> source : environment.getPropertySources()) {
            if (!(source instanceof EnumerablePropertySource<?> enumerable)) {
                continue;
            }
            for (String propertyName : enumerable.getPropertyNames()) {
                if (!propertyName.startsWith(NAMED_WEBHOOK_PREFIX)
                        || propertyName.length() == NAMED_WEBHOOK_PREFIX.length()) {
                    continue;
                }
                String targetName = propertyName.substring(NAMED_WEBHOOK_PREFIX.length())
                        .toLowerCase(Locale.ROOT)
                        .replace('_', '-');
                if (!targetName.matches("[a-z0-9][a-z0-9-]{0,39}")) {
                    log.warn("⚠️ Discord webhook 대상 이름 형식 오류 — 설정 건너뜀: {}", propertyName);
                    continue;
                }
                String value = environment.getProperty(propertyName);
                if (value == null || value.isBlank() || !isAllowedWebhook(value.trim())) {
                    log.warn("⚠️ Discord webhook 대상 URL 형식 오류 — 설정 건너뜀: {}", targetName);
                    continue;
                }
                configured.putIfAbsent(targetName, value.trim());
            }
        }
        return Map.copyOf(configured);
    }

    private String safeTargetName(String targetName) {
        if (targetName == null || targetName.isBlank()) {
            return null;
        }
        String normalized = targetName.trim().toLowerCase(Locale.ROOT);
        return normalized.matches("default|[a-z0-9][a-z0-9-]{0,39}") ? normalized : null;
    }

    private void postMessage(String message, String target) {
        String content = message.length() > MAX_CONTENT
                ? message.substring(0, MAX_CONTENT - 3) + "..." : message;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(Map.of("content", content), headers);
        restTemplate.postForObject(target, entity, String.class);
        log.info("📢 Discord 메시지 전송 완료");
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
