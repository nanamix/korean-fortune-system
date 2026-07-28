package com.fortune.ai;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiNarrationValidatorTest {
    private final AiNarrationValidator validator = new AiNarrationValidator();

    @Test
    void acceptsNarrationAlignedWithSajuFacts() {
        AiPromptRequest request = request(AiFactPacket.forSaju(SajuResult.builder()
                .yearPillar("신유")
                .monthPillar("신묘")
                .dayPillar("정유")
                .timePillar("신축")
                .dayMaster("정")
                .build()));

        AiNarrationValidator.ValidationResult result =
                validator.validate(request, "정화(丁) 일간과 정유 일주의 강점을 차분히 활용하세요.");

        assertThat(result.valid()).isTrue();
        assertThat(result.code()).isEqualTo("OK");
    }

    @Test
    void rejectsConflictingDayMasterAndPillar() {
        AiPromptRequest request = request(AiFactPacket.forSaju(SajuResult.builder()
                .yearPillar("경오")
                .monthPillar("신사")
                .dayPillar("갑자")
                .timePillar("신미")
                .dayMaster("갑")
                .build()));

        assertThat(validator.validate(request, "정화(丁) 일간입니다.").code())
                .isEqualTo("FACT_ALIGNMENT_FAILED");
        assertThat(validator.validate(request, "일주는 정유입니다.").code())
                .isEqualTo("FACT_ALIGNMENT_FAILED");
    }

    @Test
    void rejectsConflictingScoreAndDangerousHtml() {
        AiPromptRequest request = request(AiFactPacket.forDaily(DailyFortuneResult.builder()
                .totalScore(82)
                .build()));

        assertThat(validator.validate(request, "오늘의 종합 점수는 100점입니다.").code())
                .isEqualTo("FACT_ALIGNMENT_FAILED");
        assertThat(validator.validate(request, "<script>alert(1)</script>").code())
                .isEqualTo("UNSAFE_HTML");
    }

    private AiPromptRequest request(AiFactPacket packet) {
        return new AiPromptRequest("test-model", "system", packet.promptBlock(), 0.5, packet);
    }
}
