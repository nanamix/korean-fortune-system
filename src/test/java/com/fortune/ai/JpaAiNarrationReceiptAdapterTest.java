package com.fortune.ai;

import com.fortune.entity.SecurityAuditLog;
import com.fortune.repository.SecurityAuditLogRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class JpaAiNarrationReceiptAdapterTest {

    @Test
    void storesOnlyPrivacySafeReceiptMetadata() {
        SecurityAuditLogRepository repository = mock(SecurityAuditLogRepository.class);
        JpaAiNarrationReceiptAdapter adapter =
                new JpaAiNarrationReceiptAdapter(repository, new ObjectMapper());
        AiNarrationReceipt receipt = new AiNarrationReceipt(
                "fortune-fact-packet/v1",
                "engine-v3",
                "saju",
                "a".repeat(64),
                "deepseek",
                "deepseek-v4-flash",
                true,
                false,
                true,
                "FACT_ALIGNMENT_FAILED");

        adapter.record(receipt);

        ArgumentCaptor<SecurityAuditLog> captor = ArgumentCaptor.forClass(SecurityAuditLog.class);
        verify(repository).save(captor.capture());
        SecurityAuditLog stored = captor.getValue();
        assertThat(stored.getAction()).isEqualTo(JpaAiNarrationReceiptAdapter.ACTION);
        assertThat(stored.getResource()).isEqualTo("saju");
        assertThat(stored.getSuccess()).isFalse();
        assertThat(stored.getDetails())
                .contains("\"factHash\":\"" + "a".repeat(64) + "\"")
                .contains("\"fallbackUsed\":true")
                .doesNotContain("birth", "question", "response", "name", "gender");
    }
}
