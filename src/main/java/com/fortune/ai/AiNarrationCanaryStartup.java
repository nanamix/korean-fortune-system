package com.fortune.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 운영 활성화 시 application ready 이후 synthetic canary를 1회 실행한다.
 */
@Slf4j
@Component
public class AiNarrationCanaryStartup {
    private final AiCanaryProperties properties;
    private final AiNarrationCanaryService canaryService;

    public AiNarrationCanaryStartup(
            AiCanaryProperties properties,
            AiNarrationCanaryService canaryService) {
        this.properties = properties;
        this.canaryService = canaryService;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void runAfterStartup() {
        if (!properties.enabled() || !properties.runOnStartup()) {
            return;
        }
        AiNarrationCanaryService.Result result =
                canaryService.run(AiNarrationCanaryService.CONFIRMATION);
        log.info("Synthetic AI canary completed; state={}, reason={}, fixture={}",
                result.state(), result.reasonCode(), result.fixtureId());
    }
}
