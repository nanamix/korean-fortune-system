package com.fortune.ai;

import java.time.Instant;
import java.util.List;

public record AiProviderStatus(
        String state,
        String provider,
        String model,
        boolean enabled,
        boolean apiKeyConfigured,
        boolean fallbackAvailable,
        String reasonCode,
        String reason,
        Instant lastAttemptAt,
        List<ProviderOption> compatibleProviders) {

    public record ProviderOption(
            String id,
            String name,
            String connection,
            List<String> models) {
    }
}
