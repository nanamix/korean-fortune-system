package com.fortune.ai;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuResult;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.ZodiacFortuneResult;
import org.springframework.stereotype.Component;

@Component
public class FallbackFortuneInterpreter {

    public String answerQuestion(SajuResult sajuResult, String question) {
        String dayMaster = safe(sajuResult.getDayMaster(), "일간");
        return """
                질문 요약
                %s

                사주 관점의 기본 해석
                %s 일간은 %s한 성향을 활용해 문제를 단계적으로 풀 때 강점이 살아납니다. 현재는 질문의 결과를 단정하기보다 선택 가능한 조건을 나누어 검토하는 편이 좋습니다.

                현실적인 행동 제안
                1. 원하는 결과와 피하고 싶은 조건을 각각 한 문장으로 적으세요.
                2. 이번 주에 확인할 수 있는 작은 행동 하나를 정해 실행하세요.
                3. 결과를 날짜와 사실 중심으로 기록한 뒤 다음 결정을 조정하세요.

                주의할 점
                이 내용은 외부 AI 제공자를 사용하지 않은 규칙 기반 기본 해석입니다. 중요한 건강·법률·재정 결정은 전문가와 객관적인 자료를 우선하세요.
                """.formatted(safe(question, "질문 없음"), dayMaster, getStemCharacteristic(dayMaster));
    }

    public String interpretSaju(SajuResult sajuResult) {
        String dayMaster = safe(sajuResult.getDayMaster(), "일간");
        return """
                사주 기본 해석 (AI 폴백)

                %s 일간은 %s한 성향을 바탕으로 자기 리듬을 만들어갈 때 강점이 살아납니다.
                지금은 큰 결론을 서두르기보다 장점을 작게 반복해서 신뢰를 쌓는 흐름이 좋습니다.

                조언: 본인의 강점을 살려 단계적으로 목표를 달성해 나가세요.
                """.formatted(dayMaster, getStemCharacteristic(dayMaster));
    }

    public String generateDailyAdvice(DailyFortuneResult dailyFortune) {
        int score = dailyFortune.getTotalScore();
        String advice = score >= 70 ? "적극적으로 움직이기 좋은 날입니다." :
                score >= 50 ? "무난한 흐름이니 루틴을 지키는 편이 좋습니다." :
                        "속도를 낮추고 확인을 늘리는 편이 좋습니다.";
        return "%s 일일 운세 (AI 폴백): 종합 점수 %d점. %s"
                .formatted(dailyFortune.getDate(), score, advice);
    }

    public String generateZodiacAdvice(ZodiacFortuneResult zodiacResult) {
        return "%s 별자리 운세 (AI 폴백): %s 색이 흐름을 가볍게 만들어줄 수 있습니다."
                .formatted(safe(zodiacResult.getZodiacKoreanName(), "해당 별자리"), safe(zodiacResult.getLuckyColor(), "밝은"));
    }

    public String generateTojeongAdvice(TojeongResult tojeongResult) {
        return "토정비결 %d번 괘 %s (AI 폴백): %s"
                .formatted(tojeongResult.getGwaNumber(), safe(tojeongResult.getGwaName(), "이름 없음"), safe(tojeongResult.getAdvice(), "꾸준함을 유지하세요."));
    }

    private String getStemCharacteristic(String dayMaster) {
        return switch (dayMaster) {
            case "갑" -> "정직하고 꿋꿋";
            case "을" -> "섬세하고 유연";
            case "병" -> "밝고 활동적";
            case "정" -> "따뜻하고 정성스러운";
            case "무" -> "든든하고 포용력 있는";
            case "기" -> "실용적이고 현실적";
            case "경" -> "원칙적이고 공정";
            case "신" -> "예리하고 세련된";
            case "임" -> "깊고 포용력 넓은";
            case "계" -> "순수하고 지혜로운";
            default -> "균형 잡힌";
        };
    }

    private static String safe(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
