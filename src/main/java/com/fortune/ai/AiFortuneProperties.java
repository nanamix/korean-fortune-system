package com.fortune.ai;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.fortune.ai")
public record AiFortuneProperties(
        boolean enabled,
        String provider,
        String model,
        String baseUrl,
        String apiKey,
        Duration timeout,
        boolean fallbackEnabled
) {
    public AiFortuneProperties {
        provider = isBlank(provider) ? "fallback" : provider;
        model = isBlank(model) ? "deepseek-v4-flash" : model;
        baseUrl = isBlank(baseUrl) ? "https://api.deepseek.com" : baseUrl;
        apiKey = apiKey == null ? "" : apiKey;
        timeout = timeout == null ? Duration.ofSeconds(30) : timeout;
    }

    public boolean providerCallsEnabled() {
        return enabled && !"fallback".equalsIgnoreCase(provider);
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
