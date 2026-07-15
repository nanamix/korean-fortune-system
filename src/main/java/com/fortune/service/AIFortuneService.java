package com.fortune.service;

import com.fortune.ai.AiFortuneFacade;
import com.fortune.ai.AiProviderStatus;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuResult;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.ZodiacFortuneResult;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Backward-compatible adapter for fortune AI endpoints.
 */
@Slf4j
@Service
public class AIFortuneService {
    private final AiFortuneFacade aiFortuneFacade;

    public AIFortuneService(AiFortuneFacade aiFortuneFacade) {
        this.aiFortuneFacade = aiFortuneFacade;
    }

    @Cacheable(value = "ai-saju-interpretation", key = "#sajuResult.dayMaster + '_' + #sajuResult.dayPillar")
    public String interpretSaju(SajuResult sajuResult) {
        log.info("AI saju interpretation requested: {}", sajuResult.getDayMaster());
        return aiFortuneFacade.interpretSaju(sajuResult);
    }

    @Cacheable(value = "ai-daily-advice", key = "#dailyFortune.date + '_' + #dailyFortune.totalScore")
    public String generateDailyAdvice(DailyFortuneResult dailyFortune) {
        log.info("AI daily advice requested: {}", dailyFortune.getDate());
        return aiFortuneFacade.generateDailyAdvice(dailyFortune);
    }

    @Cacheable(value = "ai-zodiac-advice", key = "#zodiacResult.zodiac + '_' + #zodiacResult.targetDate")
    public String generateZodiacAdvice(ZodiacFortuneResult zodiacResult) {
        log.info("AI zodiac advice requested: {}", zodiacResult.getZodiacKoreanName());
        return aiFortuneFacade.generateZodiacAdvice(zodiacResult);
    }

    @Cacheable(value = "ai-tojeong-advice", key = "#tojeongResult.gwaNumber + '_' + #tojeongResult.targetYear")
    public String generateTojeongAdvice(TojeongResult tojeongResult) {
        log.info("AI tojeong advice requested: {}", tojeongResult.getGwaName());
        return aiFortuneFacade.generateTojeongAdvice(tojeongResult);
    }

    public String answerFortuneQuestion(SajuResult saju, String question) {
        return aiFortuneFacade.answerQuestion(saju, question);
    }

    public AiProviderStatus providerStatus() {
        return aiFortuneFacade.providerStatus();
    }

    public CompletableFuture<String> analyzeFortuneAsync(SajuResult saju, String analysisType) {
        return CompletableFuture.supplyAsync(() -> switch (analysisType.toLowerCase()) {
            case "comprehensive" -> "종합 분석: " + aiFortuneFacade.interpretSaju(saju);
            case "yearly" -> "연간 예측: " + aiFortuneFacade.interpretSaju(saju);
            case "compatibility" -> "궁합 분석: " + aiFortuneFacade.interpretSaju(saju);
            default -> aiFortuneFacade.interpretSaju(saju);
        });
    }
}
