package com.fortune.ai;

import com.fortune.repository.SecurityAuditLogRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiNarrationReceiptOperationsTest {
    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-07-28T03:00:00Z"), ZoneId.of("Asia/Seoul"));

    @Test
    void summarizesPrivacySafeLowCardinalityFields() {
        SecurityAuditLogRepository repository = mock(SecurityAuditLogRepository.class);
        AiNarrationReceiptOperations operations = new AiNarrationReceiptOperations(
                repository, new AiReceiptProperties(false, 90, 7), CLOCK);
        when(repository.countByActionAndTimestampAfter(any(), any())).thenReturn(12L);
        when(repository.countByActionAndSuccessAndTimestampAfter(any(), eq(true), any()))
                .thenReturn(8L);
        when(repository.countByActionAndSuccessAndTimestampAfter(any(), eq(false), any()))
                .thenReturn(4L);
        when(repository.countByActionGroupedByResource(any(), any()))
                .thenReturn(List.of(new Object[]{"saju", 7L}, new Object[]{"daily", 5L}));

        var summary = operations.summarize(30);

        assertThat(summary.windowDays()).isEqualTo(30);
        assertThat(summary.from()).isEqualTo(LocalDateTime.of(2026, 6, 28, 12, 0));
        assertThat(summary.total()).isEqualTo(12);
        assertThat(summary.accepted()).isEqualTo(8);
        assertThat(summary.fallbackOrRejected()).isEqualTo(4);
        assertThat(summary.byDomain()).containsExactly(
                org.assertj.core.api.Assertions.entry("saju", 7L),
                org.assertj.core.api.Assertions.entry("daily", 5L));
    }

    @Test
    void leavesRetentionDisabledUntilOperationalApproval() {
        SecurityAuditLogRepository repository = mock(SecurityAuditLogRepository.class);
        AiNarrationReceiptOperations operations = new AiNarrationReceiptOperations(
                repository, new AiReceiptProperties(false, 90, 7), CLOCK);

        assertThat(operations.cleanupExpired()).isZero();
        verify(repository, never()).deleteByActionAndTimestampBefore(any(), any());
    }

    @Test
    void deletesOnlyExpiredNarrationReceiptsWhenEnabled() {
        SecurityAuditLogRepository repository = mock(SecurityAuditLogRepository.class);
        when(repository.deleteByActionAndTimestampBefore(any(), any())).thenReturn(3L);
        AiNarrationReceiptOperations operations = new AiNarrationReceiptOperations(
                repository, new AiReceiptProperties(true, 90, 7), CLOCK);

        assertThat(operations.cleanupExpired()).isEqualTo(3);
        verify(repository).deleteByActionAndTimestampBefore(
                JpaAiNarrationReceiptAdapter.ACTION,
                LocalDateTime.of(2026, 4, 29, 12, 0));
    }
}
