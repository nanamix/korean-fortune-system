package com.fortune.ai;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiNarrationCanaryStartupTest {

    @Test
    void runsApprovedSyntheticCanaryAfterStartup() {
        AiNarrationCanaryService service = mock(AiNarrationCanaryService.class);
        when(service.run(AiNarrationCanaryService.CONFIRMATION))
                .thenReturn(new AiNarrationCanaryService.Result(
                        "PASSED", "OK", AiNarrationCanaryService.FIXTURE_ID,
                        "deepseek", "deepseek-v4-flash", "a".repeat(64), null));
        AiNarrationCanaryStartup startup = new AiNarrationCanaryStartup(
                new AiCanaryProperties(true, true), service);

        startup.runAfterStartup();

        verify(service).run(AiNarrationCanaryService.CONFIRMATION);
    }

    @Test
    void staysInactiveOutsideApprovedProductionConfiguration() {
        AiNarrationCanaryService service = mock(AiNarrationCanaryService.class);
        AiNarrationCanaryStartup startup = new AiNarrationCanaryStartup(
                new AiCanaryProperties(true, false), service);

        startup.runAfterStartup();

        verify(service, never()).run(AiNarrationCanaryService.CONFIRMATION);
    }
}
