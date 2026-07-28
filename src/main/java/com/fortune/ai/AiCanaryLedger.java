package com.fortune.ai;

import com.fortune.entity.SecurityAuditLog;
import com.fortune.repository.SecurityAuditLogRepository;
import java.time.LocalDateTime;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

/**
 * 동일 synthetic fixture와 엔진 계약의 canary 중복 실행을 막는 영수증.
 */
@Component
public class AiCanaryLedger {
    static final String ACTION = "AI_NARRATION_CANARY";

    private final SecurityAuditLogRepository repository;
    private final ObjectMapper objectMapper;

    public AiCanaryLedger(
            SecurityAuditLogRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    public boolean completed(String fixtureId) {
        return repository.existsByActionAndResource(ACTION, resource(fixtureId));
    }

    public void record(AiNarrationCanaryService.Result result) {
        repository.save(SecurityAuditLog.builder()
                .action(ACTION)
                .resource(resource(result.fixtureId()))
                .success("PASSED".equals(result.state()))
                .timestamp(LocalDateTime.now())
                .details(objectMapper.writeValueAsString(result))
                .build());
    }

    private String resource(String fixtureId) {
        return fixtureId + ":" + AiFactPacket.ENGINE_VERSION;
    }
}
