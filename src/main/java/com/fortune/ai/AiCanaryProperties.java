package com.fortune.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 실제 provider synthetic canary의 명시적 운영 gate.
 */
@ConfigurationProperties(prefix = "app.fortune.ai.canary")
public record AiCanaryProperties(
        boolean enabled,
        boolean runOnStartup) {
}
