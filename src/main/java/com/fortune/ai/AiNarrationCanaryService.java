package com.fortune.ai;

import com.fortune.dto.SajuResult;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 개인정보 없이 실제 provider 연결과 narration gate를 함께 확인하는 synthetic canary.
 */
@Slf4j
@Service
public class AiNarrationCanaryService {
    static final String CONFIRMATION = "RUN_SYNTHETIC_AI_CANARY";
    static final String FIXTURE_ID = "synthetic-saju-1981-03-20-v1";

    private final AiCanaryProperties canaryProperties;
    private final AiFortuneFacade aiFortuneFacade;
    private final AiCanaryLedger ledger;
    private final AtomicBoolean running = new AtomicBoolean();

    public AiNarrationCanaryService(
            AiCanaryProperties canaryProperties,
            AiFortuneFacade aiFortuneFacade,
            AiCanaryLedger ledger) {
        this.canaryProperties = canaryProperties;
        this.aiFortuneFacade = aiFortuneFacade;
        this.ledger = ledger;
    }

    public Preview preview() {
        AiProviderStatus status = aiFortuneFacade.providerStatus();
        return new Preview(
                canaryProperties.enabled(),
                FIXTURE_ID,
                status.provider(),
                status.model(),
                status.enabled(),
                status.apiKeyConfigured(),
                CONFIRMATION);
    }

    public Result run(String confirmation) {
        Instant attemptedAt = Instant.now();
        if (!canaryProperties.enabled()) {
            return result("DISABLED", "CANARY_DISABLED", "", attemptedAt);
        }
        if (!CONFIRMATION.equals(confirmation)) {
            return result("BLOCKED", "CONFIRMATION_REQUIRED", "", attemptedAt);
        }
        AiProviderStatus before = aiFortuneFacade.providerStatus();
        if (!before.enabled() || !before.apiKeyConfigured()) {
            return result("BLOCKED", "PROVIDER_NOT_READY", "", attemptedAt);
        }
        if (ledger.completed(FIXTURE_ID)) {
            return result("SKIPPED", "ALREADY_COMPLETED", "", attemptedAt);
        }
        if (!running.compareAndSet(false, true)) {
            return result("BUSY", "CANARY_ALREADY_RUNNING", "", attemptedAt);
        }
        try {
            SajuResult fixture = SajuResult.builder()
                    .yearPillar("신유")
                    .monthPillar("신묘")
                    .dayPillar("정유")
                    .timePillar("신축")
                    .dayMaster("정")
                    .fortuneSummary("합성 fixture: 엔진 사실을 변경하지 않고 실천 조언만 서술")
                    .build();
            String factHash = AiFactPacket.forSaju(fixture).factHash();
            aiFortuneFacade.answerQuestion(
                    fixture,
                    "합성 canary입니다. 엔진 사실을 바꾸지 말고 핵심 성향과 실천 조언을 요약하세요.");
            AiProviderStatus after = aiFortuneFacade.providerStatus();
            boolean passed = "AVAILABLE".equals(after.state()) && "OK".equals(after.reasonCode());
            Result result = result(
                    passed ? "PASSED" : "FAILED",
                    after.reasonCode(),
                    factHash,
                    attemptedAt);
            if (passed) {
                try {
                    ledger.record(result);
                } catch (Exception exception) {
                    log.warn("Synthetic AI canary ledger write failed; fixture={}", FIXTURE_ID);
                    return result(
                            "FAILED",
                            "LEDGER_WRITE_FAILED",
                            factHash,
                            attemptedAt);
                }
            }
            return result;
        } finally {
            running.set(false);
        }
    }

    private Result result(String state, String reasonCode, String factHash, Instant attemptedAt) {
        AiProviderStatus status = aiFortuneFacade.providerStatus();
        return new Result(
                state,
                reasonCode,
                FIXTURE_ID,
                status.provider(),
                status.model(),
                factHash,
                attemptedAt);
    }

    public record Preview(
            boolean enabled,
            String fixtureId,
            String provider,
            String model,
            boolean providerEnabled,
            boolean apiKeyConfigured,
            String requiredConfirmation) {
    }

    public record Result(
            String state,
            String reasonCode,
            String fixtureId,
            String provider,
            String model,
            String factHash,
            Instant attemptedAt) {
    }
}
