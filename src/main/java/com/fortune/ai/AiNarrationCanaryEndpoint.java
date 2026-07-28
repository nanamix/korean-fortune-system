package com.fortune.ai;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.stereotype.Component;

/**
 * 보호된 Actuator 경로의 synthetic AI canary 제어면.
 */
@Component
@Endpoint(id = "aiNarrationCanary")
public class AiNarrationCanaryEndpoint {
    private final AiNarrationCanaryService canaryService;

    public AiNarrationCanaryEndpoint(AiNarrationCanaryService canaryService) {
        this.canaryService = canaryService;
    }

    @ReadOperation
    public AiNarrationCanaryService.Preview preview() {
        return canaryService.preview();
    }

    @WriteOperation
    public AiNarrationCanaryService.Result run(String confirmation) {
        return canaryService.run(confirmation);
    }
}
