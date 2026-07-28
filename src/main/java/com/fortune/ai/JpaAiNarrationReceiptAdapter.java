package com.fortune.ai;

import com.fortune.entity.SecurityAuditLog;
import com.fortune.repository.SecurityAuditLogRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * 기존 보안 감사 로그 테이블에 개인정보 비포함 AI 실행 영수증을 저장한다.
 */
@Component
public class JpaAiNarrationReceiptAdapter implements AiNarrationReceiptPort {
    static final String ACTION = "AI_NARRATION_RECEIPT";

    private final SecurityAuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public JpaAiNarrationReceiptAdapter(
            SecurityAuditLogRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Override
    public void record(AiNarrationReceipt receipt) {
        String details = objectMapper.writeValueAsString(receipt);
        repository.save(SecurityAuditLog.builder()
                .action(ACTION)
                .resource(receipt.domain())
                .success(receipt.accepted())
                .timestamp(LocalDateTime.now())
                .details(details)
                .build());
    }
}
