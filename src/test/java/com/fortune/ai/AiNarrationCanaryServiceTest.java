package com.fortune.ai;

import com.fortune.dto.SajuResult;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiNarrationCanaryServiceTest {

    @Test
    void doesNotCallProviderWhileOperationalGateIsDisabled() {
        AiFortuneFacade facade = mock(AiFortuneFacade.class);
        AiCanaryLedger ledger = mock(AiCanaryLedger.class);
        when(facade.providerStatus()).thenReturn(status("CONFIGURED", "NOT_TESTED"));
        AiNarrationCanaryService service = new AiNarrationCanaryService(
                new AiCanaryProperties(false, false), facade, ledger);

        var result = service.run(AiNarrationCanaryService.CONFIRMATION);

        assertThat(result.state()).isEqualTo("DISABLED");
        verify(facade, never()).answerQuestion(
                org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    void requiresExactConfirmationBeforeSyntheticProviderCall() {
        AiFortuneFacade facade = mock(AiFortuneFacade.class);
        AiCanaryLedger ledger = mock(AiCanaryLedger.class);
        when(facade.providerStatus()).thenReturn(status("CONFIGURED", "NOT_TESTED"));
        AiNarrationCanaryService service = new AiNarrationCanaryService(
                new AiCanaryProperties(true, false), facade, ledger);

        var result = service.run("yes");

        assertThat(result.state()).isEqualTo("BLOCKED");
        assertThat(result.reasonCode()).isEqualTo("CONFIRMATION_REQUIRED");
        verify(facade, never()).answerQuestion(
                org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    void runsOnlySyntheticFixtureAndReturnsNoModelContent() {
        AiFortuneFacade facade = mock(AiFortuneFacade.class);
        AiCanaryLedger ledger = mock(AiCanaryLedger.class);
        SajuResult fixture = SajuResult.builder()
                .yearPillar("신유")
                .monthPillar("신묘")
                .dayPillar("정유")
                .timePillar("신축")
                .dayMaster("정")
                .fortuneSummary("합성 fixture")
                .build();
        when(facade.answerQuestion(any(), anyString())).thenReturn("외부 모델 원문");
        when(facade.providerStatus())
                .thenReturn(status("CONFIGURED", "NOT_TESTED"))
                .thenReturn(status("AVAILABLE", "OK"))
                .thenReturn(status("AVAILABLE", "OK"));
        AiNarrationCanaryService service = new AiNarrationCanaryService(
                new AiCanaryProperties(true, false), facade, ledger);

        var result = service.run(AiNarrationCanaryService.CONFIRMATION);

        assertThat(result.state()).isEqualTo("PASSED");
        assertThat(result.factHash()).hasSize(64);
        assertThat(result.toString()).doesNotContain("외부 모델 원문");
        verify(facade).answerQuestion(
                org.mockito.ArgumentMatchers.argThat(actual ->
                        actual.getDayPillar().equals(fixture.getDayPillar())
                                && actual.getDayMaster().equals(fixture.getDayMaster())),
                eq("합성 canary입니다. 엔진 사실을 바꾸지 말고 핵심 성향과 실천 조언을 요약하세요."));
        verify(ledger).record(result);
    }

    @Test
    void skipsProviderWhenTheSameFixtureAndEngineAlreadyPassed() {
        AiFortuneFacade facade = mock(AiFortuneFacade.class);
        AiCanaryLedger ledger = mock(AiCanaryLedger.class);
        when(facade.providerStatus()).thenReturn(status("CONFIGURED", "NOT_TESTED"));
        when(ledger.completed(AiNarrationCanaryService.FIXTURE_ID)).thenReturn(true);
        AiNarrationCanaryService service = new AiNarrationCanaryService(
                new AiCanaryProperties(true, true), facade, ledger);

        var result = service.run(AiNarrationCanaryService.CONFIRMATION);

        assertThat(result.state()).isEqualTo("SKIPPED");
        assertThat(result.reasonCode()).isEqualTo("ALREADY_COMPLETED");
        verify(facade, never()).answerQuestion(
                org.mockito.ArgumentMatchers.any(), anyString());
    }

    @Test
    void failsTheCanaryGateWhenIdempotencyReceiptCannotBeStored() {
        AiFortuneFacade facade = mock(AiFortuneFacade.class);
        AiCanaryLedger ledger = mock(AiCanaryLedger.class);
        when(facade.answerQuestion(any(), anyString())).thenReturn("외부 모델 원문");
        when(facade.providerStatus())
                .thenReturn(status("CONFIGURED", "NOT_TESTED"))
                .thenReturn(status("AVAILABLE", "OK"))
                .thenReturn(status("AVAILABLE", "OK"));
        doThrow(new IllegalStateException("db unavailable"))
                .when(ledger).record(any());
        AiNarrationCanaryService service = new AiNarrationCanaryService(
                new AiCanaryProperties(true, true), facade, ledger);

        var result = service.run(AiNarrationCanaryService.CONFIRMATION);

        assertThat(result.state()).isEqualTo("FAILED");
        assertThat(result.reasonCode()).isEqualTo("LEDGER_WRITE_FAILED");
    }

    private AiProviderStatus status(String state, String reasonCode) {
        return new AiProviderStatus(
                state,
                "deepseek",
                "deepseek-v4-flash",
                true,
                true,
                true,
                reasonCode,
                "test",
                null,
                List.of());
    }
}
