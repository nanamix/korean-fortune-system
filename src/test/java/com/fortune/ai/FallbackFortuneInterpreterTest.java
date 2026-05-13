package com.fortune.ai;

import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FallbackFortuneInterpreterTest {

    @Test
    void createsDeterministicSajuFallback() {
        FallbackFortuneInterpreter interpreter = new FallbackFortuneInterpreter();
        SajuResult saju = SajuResult.builder()
                .dayMaster("갑")
                .dayPillar("갑자")
                .build();

        String result = interpreter.interpretSaju(saju);

        assertThat(result).contains("사주 기본 해석", "갑", "AI 폴백");
        assertThat(result).contains("단계적으로 목표를 달성");
    }
}
