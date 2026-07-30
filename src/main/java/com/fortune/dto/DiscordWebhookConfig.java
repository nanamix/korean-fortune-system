package com.fortune.dto;

import java.util.List;

/**
 * 비밀 URL을 노출하지 않고 브라우저가 선택할 수 있는 Discord webhook 설정 상태.
 */
public record DiscordWebhookConfig(
        boolean defaultConfigured,
        List<String> targets) {
}
