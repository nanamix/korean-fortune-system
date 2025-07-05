package com.fortune.service;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import com.fortune.dto.SajuResult;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.dto.TojeongResult;
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.CompletableFuture;
/**
 * 🤖 AI 기반 운세 해석 서비스
 *
 * <p>AI 의존성 문제를 피하고 폴백 기능만 제공하는 안전한 버전입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.fortune.ai.enabled", havingValue = "true")
public class AIFortuneService {
    /**
     * 🎯 사주 결과 AI 해석 (폴백 버전)
     * <p>AI 서비스가 활성화되어 있지만, 실제 구현은 폴백 모드로 동작합니다.</p>
     * <p>이 메서드는 캐시를 사용하여 동일한 사주 결과에 대한 해석을 재사용합니다.</p>
     * <p>캐시 키는 일간과 일주를 조합하여 생성됩니다.</p>
     * <p>이렇게 함으로써, 동일한 사주 결과에 대한 해석을 반복적으로 요청할 때 성능을 향상시킵니다.</p>
     * <p>AI 서비스가 활성화된 경우에도, 실제 AI 호출 없이 폴백 모드로 동작합니다.</p>
     * @param sajuResult 사주 결과 객체
     * @return
     */
    @Cacheable(value = "ai-saju-interpretation", key = "#sajuResult.dayMaster + '_' + #sajuResult.dayPillar")
    public String interpretSaju(SajuResult sajuResult) {
        log.info("🤖 AI 사주 해석 시작 (폴백 모드): {}", sajuResult.getDayMaster());
        // AI 서비스가 활성화되어 있지만 실제 구현은 폴백 사용
        return generateFallbackSajuInterpretation(sajuResult);
    }
    /**
     * 📅 일일 운세 AI 조언 생성
     * <p>AI 서비스가 활성화되어 있지만, 실제 구현은 폴백 모드로 동작합니다.</p>
     * <p>이 메서드는 캐시를 사용하여 동일한 날짜와 점수에 대한 조언을 재사용합니다.</p>
     * <p>캐시 키는 날짜와 총 점수를 조합하여 생성됩니다.</p>
     * <p>이렇게 함으로써, 동일한 날짜와 점수에 대한 조언을 반복적으로 요청할 때 성능을 향상시킵니다.</p>
     * @param dailyFortune 일일 운세 결과 객체
     * @return
     */
    @Cacheable(value = "ai-daily-advice", key = "#dailyFortune.date + '_' + #dailyFortune.totalScore")
    public String generateDailyAdvice(DailyFortuneResult dailyFortune) {
        log.info("🤖 AI 일일 조언 생성 시작 (폴백 모드): {}", dailyFortune.getDate());
        return generateFallbackDailyAdvice(dailyFortune);
    }
    /**
     * ⭐ 별자리 운세 AI 해석 (폴백 버전)
     * <p>AI 서비스가 활성화되어 있지만, 실제 구현은 폴백 모드로 동작합니다.</p>
     * <p>이 메서드는 캐시를 사용하여 동일한 별자리와 날짜에 대한 조언을 재사용합니다.</p>
     * <p>캐시 키는 별자리 이름과 날짜를 조합하여 생성됩니다.</p>
     * <p>이렇게 함으로써, 동일한 별자리와 날짜에 대한 조언을 반복적으로 요청할 때 성능을 향상시킵니다.</p>
     * @param zodiacResult 별자리 운세 결과 객체
     * @return 별자리 운세 조언
     */
    @Cacheable(value = "ai-zodiac-advice", key = "#zodiacResult.zodiac + '_' + #zodiacResult.targetDate")
    public String generateZodiacAdvice(ZodiacFortuneResult zodiacResult) {
        log.info("🤖 AI 별자리 조언 생성 시작 (폴백 모드): {}", zodiacResult.getZodiacKoreanName());
        return generateFallbackZodiacAdvice(zodiacResult);
    }
    /**
     * 📜 토정비결 AI 해석 (폴백 버전)
     * @param tojeongResult 토정비결 결과 객체
     * @return 토정비결 조언
     */
    @Cacheable(value = "ai-tojeong-advice", key = "#tojeongResult.gwaNumber + '_' + #tojeongResult.targetYear")
    public String generateTojeongAdvice(TojeongResult tojeongResult) {
        log.info("🤖 AI 토정비결 조언 생성 시작 (폴백 모드): {} 괘", tojeongResult.getGwaName());
        return generateFallbackTojeongAdvice(tojeongResult);
    }
    /**
     * ❓ 자연어 운세 질문 답변 (폴백 버전)
     * @param saju 사주 결과 객체
     * @param question 질문
     * @return 자연어 운세 질문 답변
     */
    public String answerFortuneQuestion(SajuResult saju, String question) {
        log.info("🤖 AI 질문 답변 시작 (폴백 모드): {}", question);
        return """
            죄송합니다. 현재 AI 서비스가 폴백 모드로 동작중입니다.
            간단히 말씀드리면, %s 일간이신 분은 %s의 특성을 가지고 계시니, 
            이를 바탕으로 현명한 판단을 하시기 바랍니다.
            질문: %s
            답변: 사주학적 관점에서 보면, 개인의 노력과 선택이 가장 중요합니다. 🙏
            """.formatted(saju.getDayMaster(), getStemCharacteristic(saju.getDayMaster()), question);
    }
    /**
     * 🔄 비동기 운세 분석 (폴백 버전)
     * @param saju 사주 결과 객체
     * @param analysisType 분석 유형
     * @return 비동기 운세 분석 결과
     */
    public CompletableFuture<String> analyzeFortuneAsync(SajuResult saju, String analysisType) {
        return CompletableFuture.supplyAsync(() -> {
            return switch (analysisType.toLowerCase()) {
                case "comprehensive" -> "종합 분석: " + generateFallbackSajuInterpretation(saju);
                case "yearly" -> "연간 예측: " + saju.getDayMaster() + " 일간의 특성을 바탕으로 꾸준한 노력이 필요합니다.";
                case "compatibility" -> "궁합 분석: " + saju.getDayMaster() + " 일간의 특성에 맞는 상대를 찾아보세요.";
                default -> generateFallbackSajuInterpretation(saju);
            };
        });
    }
    /**
     * 사주 기본 해석 (AI 폴백 모드)
     * @param sajuResult 사주 결과 객체
     * @return 사주 기본 해석
     */
    private String generateFallbackSajuInterpretation(SajuResult sajuResult) {
        return """
            🔮 사주 기본 해석 (AI 폴백 모드)
            📊 사주 정보: %s
            👤 일간: %s
            이 사주는 %s의 기운을 가지고 있어 %s한 성향을 보입니다.
            타고난 장점을 발휘하시면 좋은 결과가 있을 것입니다.
            💡 %s 일간의 특징:
            - 긍정적 특성: %s
            - 주의할 점: 과도한 성급함을 피하고 꾸준함을 유지하세요
            - 조언: 본인의 강점을 살려 단계적으로 목표를 달성해 나가세요
            ✨ 더 자세한 AI 해석을 원하시면 OpenAI API 키를 설정하고 AI 모드를 활성화해주세요.
            """.formatted(
                sajuResult.getFormattedSaju(),
                sajuResult.getDayMaster(),
                sajuResult.getDayMaster(),
                getStemCharacteristic(sajuResult.getDayMaster()),
                sajuResult.getDayMaster(),
                getStemCharacteristic(sajuResult.getDayMaster())
        );
    }
    /**
     * 일일 운세 조언 (AI 폴백 모드)
     * @param dailyFortune 일일 운세 결과 객체
     * @return 일일 운세 조언
     */
    private String generateFallbackDailyAdvice(DailyFortuneResult dailyFortune) {
        int score = dailyFortune.getTotalScore();
        String advice = score >= 70 ? "좋은 하루가 될 것입니다. 적극적으로 행동하세요!" :
                score >= 50 ? "무난한 하루입니다. 꾸준히 노력하세요." :
                        "조심스러운 하루입니다. 신중하게 행동하세요.";
        return """
            📅 %s 일일 운세 (폴백 모드)
            📊 종합 점수: %d/100
            💡 오늘의 조언: %s
            🎯 길방위: %s
            🌈 길한 색깔: %s
            📋 세부 조언:
            • 연애운(%d점): %s
            • 직장운(%d점): %s  
            • 건강운(%d점): %s
            • 재물운(%d점): %s
            ✨ AI 서비스 활성화 시 더 상세한 개인 맞춤 해석을 받을 수 있습니다.
            """.formatted(
                dailyFortune.getDate(),
                score,
                advice,
                dailyFortune.getLuckyDirection(),
                String.join(", ", dailyFortune.getLuckyColors()),
                dailyFortune.getCategoryFortune().getLoveScore(),
                getScoreMessage(dailyFortune.getCategoryFortune().getLoveScore()),
                dailyFortune.getCategoryFortune().getCareerScore(),
                getScoreMessage(dailyFortune.getCategoryFortune().getCareerScore()),
                dailyFortune.getCategoryFortune().getHealthScore(),
                getScoreMessage(dailyFortune.getCategoryFortune().getHealthScore()),
                dailyFortune.getCategoryFortune().getWealthScore(),
                getScoreMessage(dailyFortune.getCategoryFortune().getWealthScore())
        );
    }
    /**
     * 별자리 운세 조언 (AI 폴백 모드)
     * @param zodiacResult 별자리 운세 결과 객체
     * @return 별자리 운세 조언
     */
    private String generateFallbackZodiacAdvice(ZodiacFortuneResult zodiacResult) {
        return """
            ⭐ %s 운세 (폴백 모드)
            📅 %s
            🎨 행운의 색깔: %s
            💎 행운의 보석: %s
            🔢 행운의 숫자: %s
            💡 오늘은 %s님에게 새로운 기회가 찾아올 수 있는 날입니다.
            긍정적인 마음가짐으로 하루를 시작해보세요!
            📋 별자리 특성:
            %s
            🎯 오늘의 키워드: 성장, 기회, 긍정
            ✨ AI 서비스 활성화 시 더 자세한 별자리 분석을 받을 수 있습니다.
            """.formatted(
                zodiacResult.getZodiacKoreanName(),
                zodiacResult.getTargetDate(),
                zodiacResult.getLuckyColor(),
                zodiacResult.getLuckyStone(),
                zodiacResult.getLuckyNumbers(),
                zodiacResult.getZodiacKoreanName(),
                zodiacResult.getPersonality()
        );
    }
    /**
     * 토정비결 조언 (AI 폴백 모드)
     * @param tojeongResult 토정비결 결과 객체
     * @return 토정비결 조언
     */
    private String generateFallbackTojeongAdvice(TojeongResult tojeongResult) {
        return """
            📜 토정비결 %d번 괘: %s (폴백 모드)
            🎯 %d년 운세
            📊 종합 점수: %d/100
            📋 괘의 의미: %s
            📅 길한 달: %s
            ⚠️ 주의할 달: %s
            💡 연간 조언:
            • 상반기: 새로운 계획을 세우고 차근차근 준비하세요
            • 하반기: 준비한 것들이 결실을 맺을 수 있는 시기입니다
            • 전체적으로: 꾸준한 노력과 긍정적인 마음가짐이 중요합니다
            🎯 핵심 키워드: 인내, 성실, 지혜
            ✨ AI 서비스 활성화 시 더 현대적이고 구체적인 해석을 받을 수 있습니다.
            """.formatted(
                tojeongResult.getGwaNumber(),
                tojeongResult.getGwaName(),
                tojeongResult.getTargetYear(),
                tojeongResult.getOverallScore(),
                tojeongResult.getSummary(),
                tojeongResult.getLuckyMonths(),
                tojeongResult.getCautionMonths()
        );
    }
    /**
     * 일간 특성 조회
     * @param dayMaster 일간
     * @return 일간 특성
     */
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
            default -> "균형잡힌";
        };
    }
    /**
     * 점수 메시지 조회
     * @param score 점수
     * @return 점수 메시지
     */
    private String getScoreMessage(int score) {
        if (score >= 80) return "매우 좋음";
        else if (score >= 60) return "좋음";
        else if (score >= 40) return "보통";
        else return "주의 필요";
    }
}
