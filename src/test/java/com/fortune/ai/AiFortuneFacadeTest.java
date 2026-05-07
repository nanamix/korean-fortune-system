package com.fortune.ai;

import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AiFortuneFacadeTest {

    @Test
    void usesFallbackWhenAiIsDisabled() {
        AiFortuneProperties properties = new AiFortuneProperties(
                false,
                "openai",
                "gpt-5-mini",
                "https://api.openai.com/v1",
                "",
                null,
                true
        );
        AiFortuneFacade facade = new AiFortuneFacade(
                properties,
                new AiPromptFactory(properties),
                new FallbackFortuneInterpreter(),
                Optional.of(request -> new AiPromptResponse("provider response", "test", false))
        );

        String result = facade.interpretSaju(SajuResult.builder()
                .dayMaster("갑")
                .dayPillar("갑자")
                .build());

        assertThat(result).contains("AI 폴백");
        assertThat(result).doesNotContain("provider response");
    }

    @Test
    void fallsBackWhenProviderThrows() {
        AiFortuneProperties properties = new AiFortuneProperties(
                true,
                "openai",
                "gpt-5-mini",
                "https://api.openai.com/v1",
                "test-key",
                null,
                true
        );
        AiFortuneFacade facade = new AiFortuneFacade(
                properties,
                new AiPromptFactory(properties),
                new FallbackFortuneInterpreter(),
                Optional.of(request -> {
                    throw new IllegalStateException("provider failed");
                })
        );

        String result = facade.interpretSaju(SajuResult.builder()
                .dayMaster("갑")
                .dayPillar("갑자")
                .build());

        assertThat(result).contains("AI 폴백");
    }

    @Test
    void returnsProviderContentWhenEnabledAndProviderSucceeds() {
        AiFortuneProperties properties = new AiFortuneProperties(
                true,
                "openai",
                "gpt-5-mini",
                "https://api.openai.com/v1",
                "test-key",
                null,
                true
        );
        AiFortuneFacade facade = new AiFortuneFacade(
                properties,
                new AiPromptFactory(properties),
                new FallbackFortuneInterpreter(),
                Optional.of(request -> new AiPromptResponse("현대적 AI 해석", "openai", false))
        );

        String result = facade.interpretSaju(SajuResult.builder()
                .dayMaster("갑")
                .dayPillar("갑자")
                .build());

        assertThat(result).isEqualTo("현대적 AI 해석");
    }
}
