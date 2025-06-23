package com.fortune.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import com.fortune.dto.SajuResult;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.dto.TojeongResult;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;

/**
 * 🤖 AI 기반 운세 해석 서비스
 * 
 * <p>인공지능을 활용하여 운세 데이터를 보다 풍부하고 개인화된 
 * 해석으로 변환하는 서비스입니다.</p>
 * 
 * <h3>주요 AI 기능</h3>
 * <ul>
 *   <li>🎯 개인 맞춤형 운세 해석</li>
 *   <li>📊 복합 운세 데이터 종합 분석</li>
 *   <li>💡 실용적인 조언 및 가이드 생성</li>
 *   <li>🔮 운세 트렌드 예측 및 분석</li>
 *   <li>❓ 자연어 기반 운세 질문 응답</li>
 * </ul>
 * 
 * @author AI팀
 * @version 2.0.0
 * @since 2025-06-23
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.fortune.ai.enabled", havingValue = "true")
public class AIFortuneService {

    private final ChatClient chatClient;

    @Autowired
    public AIFortuneService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    /**
     * 🎯 사주 결과 AI 해석
     * 
     * <p>사주팔자 데이터를 AI가 분석하여 개인화된 해석을 제공합니다.</p>
     * 
     * @param sajuResult 사주 계산 결과
     * @return AI가 생성한 사주 해석
     */
    @Cacheable(value = "ai-saju-interpretation", key = "#sajuResult.dayMaster + '_' + #sajuResult.dayPillar")
    public String interpretSaju(SajuResult sajuResult) {
        log.info("🤖 AI 사주 해석 시작: {}", sajuResult.getDayMaster());
        
        try {
            String prompt = """
                다음 사주팔자 정보를 바탕으로 종합적인 해석을 제공해주세요:
                
                📊 사주 정보:
                • 연주: %s
                • 월주: %s  
                • 일주: %s
                • 시주: %s
                • 일간(본인): %s
                • 성별: %s
                
                🎯 요청사항:
                1. 이 분의 기본 성격과 기질 분석
                2. 타고난 재능과 장점
                3. 주의해야 할 약점이나 경향
                4. 인생 전반적인 운세 흐름
                5. 실용적인 삶의 조언
                
                ✨ 친근하고 희망적인 어조로 작성해주세요.
                """.formatted(
                    sajuResult.getYearPillar(),
                    sajuResult.getMonthPillar(),
                    sajuResult.getDayPillar(),
                    sajuResult.getTimePillar(),
                    sajuResult.getDayMaster(),
                    sajuResult.getGender()
                );

            return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("❌ AI 사주 해석 실패: {}", e.getMessage());
            return generateFallbackSajuInterpretation(sajuResult);
        }
    }

    /**
     * 📅 일일 운세 AI 강화
     * 
     * <p>기존 일일 운세 결과를 AI가 보완하여 더 상세하고 
     * 개인화된 해석을 제공합니다.</p>
     * 
     * @param dailyFortune 일일 운세 결과
     * @param sajuResult 사주 정보
     * @return AI가 강화한 일일 운세 해석
     */
    @Cacheable(value = "ai-daily-fortune", key = "#dailyFortune.date + '_' + #sajuResult.dayMaster")
    public String enhanceDailyFortune(DailyFortuneResult dailyFortune, SajuResult sajuResult) {
        log.info("🤖 AI 일일 운세 강화: {}", dailyFortune.getDate());
        
        try {
            String prompt = """
                다음 일일 운세 정보를 바탕으로 더욱 구체적이고 실용적인 조언을 제공해주세요:
                
                📅 날짜: %s
                📊 종합 점수: %d/100
                🎭 일주: %s
                👤 일간: %s
                
                📈 분야별 운세:
                • 종합: %d점
                • 애정: %d점  
                • 재물: %d점
                • 건강: %d점
                • 직업: %d점
                
                🎯 길방위: %s
                🌈 길한 색깔: %s
                
                💡 요청사항:
                1. 오늘 하루 전체적인 에너지와 분위기
                2. 각 분야별 구체적인 조언과 주의사항
                3. 실제 행동 가이드 (시간대별 추천)
                4. 피해야 할 것과 적극 추천하는 것
                5. 마음가짐과 태도에 대한 조언
                
                ✨ 실용적이고 따뜻한 어조로 작성해주세요.
                """.formatted(
                    dailyFortune.getDate(),
                    dailyFortune.getTotalScore(),
                    dailyFortune.getDayPillar(),
                    sajuResult.getDayMaster(),
                    dailyFortune.getCategoryFortune().getOverall(),
                    dailyFortune.getCategoryFortune().getLove(),
                    dailyFortune.getCategoryFortune().getMoney(),
                    dailyFortune.getCategoryFortune().getHealth(),
                    dailyFortune.getCategoryFortune().getCareer(),
                    dailyFortune.getLuckyDirection(),
                    String.join(", ", dailyFortune.getLuckyColors())
                );

            return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("❌ AI 일일 운세 강화 실패: {}", e.getMessage());
            return generateFallbackDailyAdvice(dailyFortune);
        }
    }

    /**
     * ⭐ 별자리 운세 AI 분석
     * 
     * <p>별자리 운세 결과를 AI가 분석하여 더 풍부한 해석을 제공합니다.</p>
     * 
     * @param zodiacResult 별자리 운세 결과
     * @return AI가 생성한 별자리 운세 분석
     */
    @Cacheable(value = "ai-zodiac-fortune", key = "#zodiacResult.zodiac + '_' + #zodiacResult.targetDate")
    public String analyzeZodiacFortune(ZodiacFortuneResult zodiacResult) {
        log.info("🤖 AI 별자리 분석: {}", zodiacResult.getZodiacKoreanName());
        
        try {
            String prompt = """
                다음 별자리 운세 정보를 바탕으로 심화 분석을 제공해주세요:
                
                ⭐ 별자리: %s
                📅 날짜: %s
                🎨 행운의 색깔: %s
                💎 행운의 보석: %s
                🔢 행운의 숫자: %s
                
                📊 오늘의 운세 점수:
                • 애정: %d점
                • 직업: %d점
                • 건강: %d점
                • 재물: %d점
                
                👥 궁합 좋은 별자리: %s
                
                💡 요청사항:
                1. 이 별자리의 오늘 전반적인 우주 에너지 흐름
                2. 각 영역별 구체적인 조언과 기회
                3. 행운의 아이템 활용법
                4. 주의해야 할 시간대나 상황
                5. 궁합 별자리와의 관계 조언
                
                ✨ 신비롭고 희망적인 어조로 작성해주세요.
                """.formatted(
                    zodiacResult.getZodiacKoreanName(),
                    zodiacResult.getTargetDate(),
                    zodiacResult.getLuckyColor(),
                    zodiacResult.getLuckyStone(),
                    zodiacResult.getLuckyNumbers(),
                    zodiacResult.getTodayFortune().getLoveScore(),
                    zodiacResult.getTodayFortune().getCareerScore(),
                    zodiacResult.getTodayFortune().getHealthScore(),
                    zodiacResult.getTodayFortune().getMoneyScore(),
                    zodiacResult.getCompatibleZodiacs().stream()
                        .map(z -> z.getKoreanName())
                        .reduce((a, b) -> a + ", " + b)
                        .orElse("없음")
                );

            return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("❌ AI 별자리 분석 실패: {}", e.getMessage());
            return generateFallbackZodiacAdvice(zodiacResult);
        }
    }

    /**
     * 📜 토정비결 AI 해석
     * 
     * <p>토정비결 괘 결과를 AI가 현대적 관점에서 해석합니다.</p>
     * 
     * @param tojeongResult 토정비결 결과
     * @return AI가 생성한 토정비결 해석
     */
    @Cacheable(value = "ai-tojeong-interpretation", key = "#tojeongResult.gwaNumber + '_' + #tojeongResult.targetYear")
    public String interpretTojeong(TojeongResult tojeongResult) {
        log.info("🤖 AI 토정비결 해석: {}괘", tojeongResult.getGwaNumber());
        
        try {
            String prompt = """
                다음 토정비결 괘 정보를 현대적 관점에서 해석해주세요:
                
                📜 괘 정보:
                • 괘 번호: %d번
                • 괘 이름: %s
                • 괘 상징: %s
                • 대상 연도: %d년
                • 종합 점수: %d/100
                
                📋 기본 해석:
                %s
                
                📅 길한 달: %s
                ⚠️ 주의할 달: %s
                
                💡 요청사항:
                1. 이 괘가 가진 현대적 의미와 시사점
                2. %d년 한 해 전반의 흐름과 특징
                3. 월별 구체적인 행동 지침
                4. 기회를 놓치지 않는 방법
                5. 어려움을 극복하는 지혜
                
                ✨ 전통의 지혜와 현대적 실용성을 조화롭게 작성해주세요.
                """.formatted(
                    tojeongResult.getGwaNumber(),
                    tojeongResult.getGwaName(),
                    tojeongResult.getGwaSymbol(),
                    tojeongResult.getTargetYear(),
                    tojeongResult.getOverallScore(),
                    tojeongResult.getDetailedFortune(),
                    tojeongResult.getLuckyMonths(),
                    tojeongResult.getCautionMonths(),
                    tojeongResult.getTargetYear()
                );

            return chatClient.prompt()
                .user(prompt)
                .call()
                .content();
                
        } catch (Exception e) {
            log.error("❌ AI 토정비결 해석 실패: {}", e.getMessage());
            return generateFallbackTojeongAdvice(tojeongResult);
        }
    }

    /**
     * 💬 운세 질문 응답
     * 
     * <p>사용자의 자연어 질문에 대해 AI가 운세 관련 답변을 제공합니다.</p>
     * 
     * @param question 사용자 질문
     * @param context 운세 컨텍스트 (선택사항)
     * @return AI 답변
     */
    public CompletableFuture<String> answerFortuneQuestion(String question, String context) {
        log.info("🤖 운세 질문 처리: {}", question.substring(0, Math.min(question.length(), 50)));
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String prompt = """
                    사용자의 운세 관련 질문에 전문적이고 친근하게 답변해주세요.
                    
                    ❓ 질문: %s
                    
                    📋 참고 정보: %s
                    
                    💡 답변 가이드라인:
                    • 전문적이면서도 이해하기 쉽게 설명
                    • 구체적이고 실용적인 조언 포함
                    • 긍정적이고 희망적인 방향으로 안내
                    • 과도한 미신적 해석은 피하고 건전한 관점 유지
                    • 적절한 이모지 사용으로 친근감 증대
                    
                    ✨ 따뜻하고 지혜로운 어조로 답변해주세요.
                    """.formatted(question, context != null ? context : "없음");

                return chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();
                    
            } catch (Exception e) {
                log.error("❌ AI 질문 응답 실패: {}", e.getMessage());
                return "죄송합니다. 현재 AI 서비스에 일시적인 문제가 있어 답변을 드릴 수 없습니다. 잠시 후 다시 시도해주세요. 🙏";
            }
        });
    }

    // === 폴백 메서드들 (AI 서비스 장애 시 사용) ===

    private String generateFallbackSajuInterpretation(SajuResult sajuResult) {
        return """
            🔮 사주 기본 해석
            
            📊 사주 정보: %s
            👤 일간: %s
            
            이 사주는 %s의 기운을 가지고 있어 안정적이고 꾸준한 성향을 보입니다.
            타고난 장점을 발휘하시면 좋은 결과가 있을 것입니다.
            
            ✨ 더 자세한 해석을 원하시면 AI 서비스 복구 후 다시 요청해주세요.
            """.formatted(
                sajuResult.getFormattedSaju(),
                sajuResult.getDayMaster(),
                sajuResult.getDayMaster()
            );
    }

    private String generateFallbackDailyAdvice(DailyFortuneResult dailyFortune) {
        int score = dailyFortune.getTotalScore();
        String advice = score >= 70 ? "좋은 하루가 될 것입니다. 적극적으로 행동하세요!" :
                       score >= 50 ? "무난한 하루입니다. 꾸준히 노력하세요." :
                                    "조심스러운 하루입니다. 신중하게 행동하세요.";
        
        return """
            📅 %s 일일 운세
            📊 종합 점수: %d/100
            
            💡 오늘의 조언: %s
            🎯 길방위: %s
            🌈 길한 색깔: %s
            
            ✨ AI 서비스 복구 후 더 상세한 해석을 제공받으실 수 있습니다.
            """.formatted(
                dailyFortune.getDate(),
                score,
                advice,
                dailyFortune.getLuckyDirection(),
                String.join(", ", dailyFortune.getLuckyColors())
            );
    }

    private String generateFallbackZodiacAdvice(ZodiacFortuneResult zodiacResult) {
        return """
            ⭐ %s 운세
            📅 %s
            
            🎨 행운의 색깔: %s
            💎 행운의 보석: %s
            🔢 행운의 숫자: %s
            
            💡 오늘은 %s님에게 새로운 기회가 찾아올 수 있는 날입니다.
            긍정적인 마음가짐으로 하루를 시작해보세요!
            
            ✨ AI 서비스 복구 후 더 자세한 분석을 받아보세요.
            """.formatted(
                zodiacResult.getZodiacKoreanName(),
                zodiacResult.getTargetDate(),
                zodiacResult.getLuckyColor(),
                zodiacResult.getLuckyStone(),
                zodiacResult.getLuckyNumbers(),
                zodiacResult.getZodiacKoreanName()
            );
    }

    private String generateFallbackTojeongAdvice(TojeongResult tojeongResult) {
        return """
            📜 토정비결 %d번 괘: %s
            🎯 %d년 운세
            📊 종합 점수: %d/100
            
            %s
            
            📅 길한 달: %s
            ⚠️ 주의할 달: %s
            
            💡 꾸준한 노력과 긍정적인 마음가짐으로 좋은 한 해를 만들어가세요!
            
            ✨ AI 서비스 복구 후 더 현대적이고 구체적인 해석을 받아보실 수 있습니다.
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
}