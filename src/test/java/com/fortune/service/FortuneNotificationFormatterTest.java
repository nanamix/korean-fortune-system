package com.fortune.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.FortuneByCategory;
import com.fortune.dto.MonthlyFortune;
import com.fortune.dto.SinsalInfo;
import com.fortune.dto.TojeongResult;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class FortuneNotificationFormatterTest {
    private final FortuneNotificationFormatter formatter = new FortuneNotificationFormatter();

    @Test
    void dailyMessageContainsEveryDetailedScreenSection() {
        DailyFortuneResult result = DailyFortuneResult.builder()
                .date(LocalDate.of(2026, 7, 30))
                .dayPillar("을사")
                .totalScore(87)
                .scoreBasis("일진과 개인 오행의 조화")
                .categoryFortune(FortuneByCategory.builder()
                        .loveScore(81).loveMessage("진솔한 대화가 좋습니다.")
                        .careerScore(82).careerMessage("우선순위를 지키세요.")
                        .healthScore(83).healthMessage("가벼운 운동이 좋습니다.")
                        .wealthScore(84).wealthMessage("계획 소비가 유리합니다.")
                        .build())
                .sinsals(List.of(new SinsalInfo("천을귀인", "도움을 받는 흐름", true, 8)))
                .advice("차분히 실행하세요.")
                .luckyDirection("동쪽")
                .luckyColors(List.of("파랑", "초록"))
                .caution("서두르지 마세요.")
                .build();

        String message = formatter.formatDaily(result, "마스터");

        assertThat(message)
                .contains("총점: 87점 (매우 좋음)")
                .contains("오늘의 조언: 차분히 실행하세요.")
                .contains("점수 근거: 일진과 개인 오행의 조화")
                .contains("연애운 81점", "직업운 82점", "건강운 83점", "재물운 84점")
                .contains("오늘의 신살", "천을귀인", "도움을 받는 흐름")
                .contains("방향: 동쪽", "색상: 파랑, 초록", "주의사항: 서두르지 마세요.");
    }

    @Test
    void tojeongMessageIncludesAllMonthlyResults() {
        TojeongResult result = TojeongResult.builder()
                .targetYear(2026)
                .gwaNumber(1)
                .gwaName("건위천")
                .overallScore(90)
                .monthlyFortune(List.of(
                        MonthlyFortune.builder().month(1).score(80).message("준비의 달").build(),
                        MonthlyFortune.builder().month(12).score(92).message("결실의 달").build()))
                .build();

        assertThat(formatter.formatTojeong(result, "마스터"))
                .contains("1월 · 80점 · 준비의 달")
                .contains("12월 · 92점 · 결실의 달");
    }
}
