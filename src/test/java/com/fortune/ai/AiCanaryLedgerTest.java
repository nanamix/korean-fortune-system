package com.fortune.ai;

import com.fortune.entity.SecurityAuditLog;
import com.fortune.repository.SecurityAuditLogRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiCanaryLedgerTest {

    @Test
    void usesVersionedPrivacySafeReceiptForIdempotency() {
        SecurityAuditLogRepository repository = mock(SecurityAuditLogRepository.class);
        AiCanaryLedger ledger = new AiCanaryLedger(repository, new ObjectMapper());
        String resource = AiNarrationCanaryService.FIXTURE_ID
                + ":" + AiFactPacket.ENGINE_VERSION;
        when(repository.existsByActionAndResource(AiCanaryLedger.ACTION, resource))
                .thenReturn(true);

        assertThat(ledger.completed(AiNarrationCanaryService.FIXTURE_ID)).isTrue();

        var result = new AiNarrationCanaryService.Result(
                "PASSED",
                "OK",
                AiNarrationCanaryService.FIXTURE_ID,
                "deepseek",
                "deepseek-v4-flash",
                "a".repeat(64),
                Instant.parse("2026-07-28T04:00:00Z"));
        ledger.record(result);

        ArgumentCaptor<SecurityAuditLog> captor =
                ArgumentCaptor.forClass(SecurityAuditLog.class);
        verify(repository).save(captor.capture());
        SecurityAuditLog stored = captor.getValue();
        assertThat(stored.getAction()).isEqualTo(AiCanaryLedger.ACTION);
        assertThat(stored.getResource()).isEqualTo(resource);
        assertThat(stored.getSuccess()).isTrue();
        assertThat(stored.getDetails())
                .contains("\"factHash\":\"" + "a".repeat(64) + "\"")
                .doesNotContain("birth", "question", "response", "name", "gender");
    }
}
