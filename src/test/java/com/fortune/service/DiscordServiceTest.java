package com.fortune.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DiscordService SSRF 허용 검증 테스트.
 */
public class DiscordServiceTest {

    private final DiscordService service = new DiscordService();

    @Test
    public void allowsOfficialDiscordWebhooks() {
        assertTrue(service.isAllowedWebhook("https://discord.com/api/webhooks/123/abc"));
        assertTrue(service.isAllowedWebhook("https://discordapp.com/api/webhooks/123/abc"));
        assertTrue(service.isAllowedWebhook("https://ptb.discord.com/api/webhooks/123/abc"));
    }

    @Test
    public void rejectsSsrfAndNonWebhookUrls() {
        // 내부/임의 호스트 (SSRF)
        assertFalse(service.isAllowedWebhook("http://169.254.169.254/latest/meta-data"));
        assertFalse(service.isAllowedWebhook("https://evil.com/api/webhooks/1/2"));
        // http(비-https)
        assertFalse(service.isAllowedWebhook("http://discord.com/api/webhooks/1/2"));
        // Discord 호스트지만 webhook 경로 아님
        assertFalse(service.isAllowedWebhook("https://discord.com/api/users/@me"));
        // 유사 도메인
        assertFalse(service.isAllowedWebhook("https://discord.com.evil.com/api/webhooks/1/2"));
        assertFalse(service.isAllowedWebhook("not-a-url"));
    }

    @Test
    public void unconfiguredWebhookSkipsSilently() {
        // 기본 webhook 미설정 + url 미지정 → 예외 없이 스킵
        assertDoesNotThrow(() -> service.sendMessage("테스트", null));
    }
}
