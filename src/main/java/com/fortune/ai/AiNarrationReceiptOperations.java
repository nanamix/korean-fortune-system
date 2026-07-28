package com.fortune.ai;

import com.fortune.repository.SecurityAuditLogRepository;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AI 실행 영수증 전용 보존과 저카디널리티 집계를 제공한다.
 */
@Slf4j
@Service
public class AiNarrationReceiptOperations {
    private final SecurityAuditLogRepository repository;
    private final AiReceiptProperties properties;
    private final Clock clock;

    @Autowired
    public AiNarrationReceiptOperations(
            SecurityAuditLogRepository repository,
            AiReceiptProperties properties) {
        this(repository, properties, Clock.systemDefaultZone());
    }

    AiNarrationReceiptOperations(
            SecurityAuditLogRepository repository,
            AiReceiptProperties properties,
            Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Summary summarize(Integer requestedDays) {
        int days = requestedDays == null
                ? properties.summaryWindowDays()
                : Math.max(1, Math.min(requestedDays, 365));
        LocalDateTime from = LocalDateTime.now(clock).minusDays(days);
        long total = repository.countByActionAndTimestampAfter(
                JpaAiNarrationReceiptAdapter.ACTION, from);
        long accepted = repository.countByActionAndSuccessAndTimestampAfter(
                JpaAiNarrationReceiptAdapter.ACTION, true, from);
        long fallbackOrRejected = repository.countByActionAndSuccessAndTimestampAfter(
                JpaAiNarrationReceiptAdapter.ACTION, false, from);
        Map<String, Long> byDomain = new LinkedHashMap<>();
        for (Object[] row : repository.countByActionGroupedByResource(
                JpaAiNarrationReceiptAdapter.ACTION, from)) {
            byDomain.put(String.valueOf(row[0]), ((Number) row[1]).longValue());
        }
        return new Summary(days, from, total, accepted, fallbackOrRejected, byDomain);
    }

    /**
     * 기본 비활성. 운영에서 cleanup-enabled=true로 승인된 경우에만 매일 수행한다.
     */
    @Scheduled(
            cron = "${app.fortune.ai.receipt.cleanup-cron:0 15 3 * * *}",
            zone = "Asia/Seoul")
    @Transactional
    public long cleanupExpired() {
        if (!properties.cleanupEnabled()) {
            return 0L;
        }
        LocalDateTime cutoff = LocalDateTime.now(clock).minusDays(properties.retentionDays());
        long deleted = repository.deleteByActionAndTimestampBefore(
                JpaAiNarrationReceiptAdapter.ACTION, cutoff);
        log.info("AI narration receipt retention completed; retentionDays={}, deleted={}",
                properties.retentionDays(), deleted);
        return deleted;
    }

    public record Summary(
            int windowDays,
            LocalDateTime from,
            long total,
            long accepted,
            long fallbackOrRejected,
            Map<String, Long> byDomain) {
    }
}
