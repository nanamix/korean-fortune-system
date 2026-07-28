package com.fortune.ai;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI 실행 영수증 보존 및 운영 집계 정책.
 */
@ConfigurationProperties(prefix = "app.fortune.ai.receipt")
public record AiReceiptProperties(
        boolean cleanupEnabled,
        int retentionDays,
        int summaryWindowDays
) {
    public AiReceiptProperties {
        retentionDays = retentionDays <= 0 ? 90 : Math.min(retentionDays, 3650);
        summaryWindowDays = summaryWindowDays <= 0 ? 7 : Math.min(summaryWindowDays, 365);
    }
}
