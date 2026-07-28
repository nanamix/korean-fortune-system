package com.fortune.ai;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.stereotype.Component;

/**
 * 보호된 Actuator 경로에서 개인정보 없는 영수증 집계만 조회한다.
 */
@Component
@Endpoint(id = "aiNarrationReceipts")
public class AiNarrationReceiptEndpoint {
    private final AiNarrationReceiptOperations operations;

    public AiNarrationReceiptEndpoint(AiNarrationReceiptOperations operations) {
        this.operations = operations;
    }

    @ReadOperation
    public AiNarrationReceiptOperations.Summary summary(Integer days) {
        return operations.summarize(days);
    }
}
