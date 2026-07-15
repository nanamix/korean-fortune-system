package com.fortune.ai;

import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AiQuestionFlowTest {

    @Test
    void passesQuestionToPromptAndFallback() {
        AiFortuneProperties properties = new AiFortuneProperties(false, "fallback", "test-model",
                "https://example.invalid/v1", "", Duration.ofSeconds(5), true);
        AiPromptFactory promptFactory = new AiPromptFactory(properties);
        FallbackFortuneInterpreter fallback = new FallbackFortuneInterpreter();
        SajuResult saju = SajuResult.builder().yearPillar("갑자").monthPillar("을축")
                .dayPillar("병인").timePillar("정묘").dayMaster("병").fortuneSummary("테스트 요약").build();
        String question = "향후 3개월 커리어 계획을 알려주세요.";

        assertTrue(promptFactory.forQuestion(saju, question).user().contains(question));
        assertTrue(fallback.answerQuestion(saju, question).contains(question));
        assertTrue(fallback.answerQuestion(saju, question).contains("현실적인 행동 제안"));
    }
}
