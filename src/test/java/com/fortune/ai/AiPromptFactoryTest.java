package com.fortune.ai;

import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AiPromptFactoryTest {

    @Test
    void createsKoreanSajuPromptWithConfiguredModel() {
        AiFortuneProperties properties = new AiFortuneProperties(
                true,
                "openai",
                "gpt-5-mini",
                "https://api.openai.com/v1",
                "",
                null,
                true
        );
        AiPromptFactory promptFactory = new AiPromptFactory(properties);
        SajuResult saju = SajuResult.builder()
                .yearPillar("경오")
                .monthPillar("신사")
                .dayPillar("갑자")
                .timePillar("신미")
                .dayMaster("갑")
                .fortuneSummary("목의 기운이 강한 사주")
                .build();

        AiPromptRequest prompt = promptFactory.forSaju(saju);

        assertThat(prompt.model()).isEqualTo("gpt-5-mini");
        assertThat(prompt.system()).contains("한국 전통 사주");
        assertThat(prompt.user()).contains("갑", "경오 신사 갑자 신미");
        assertThat(prompt.user()).contains("단정적인 의학, 법률, 투자 조언은 피하세요");
    }
}
