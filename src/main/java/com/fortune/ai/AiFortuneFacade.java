package com.fortune.ai;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuResult;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.ZodiacFortuneResult;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiFortuneFacade {
    private final AiFortuneProperties properties;
    private final AiPromptFactory promptFactory;
    private final FallbackFortuneInterpreter fallbackInterpreter;
    private final Optional<AiProviderPort> provider;

    public AiFortuneFacade(
            AiFortuneProperties properties,
            AiPromptFactory promptFactory,
            FallbackFortuneInterpreter fallbackInterpreter,
            Optional<AiProviderPort> provider) {
        this.properties = properties;
        this.promptFactory = promptFactory;
        this.fallbackInterpreter = fallbackInterpreter;
        this.provider = provider;
    }

    public String interpretSaju(SajuResult sajuResult) {
        return completeOrFallback(promptFactory.forSaju(sajuResult), () -> fallbackInterpreter.interpretSaju(sajuResult));
    }

    public String generateDailyAdvice(DailyFortuneResult dailyFortune) {
        return completeOrFallback(promptFactory.forDaily(dailyFortune), () -> fallbackInterpreter.generateDailyAdvice(dailyFortune));
    }

    public String generateZodiacAdvice(ZodiacFortuneResult zodiacResult) {
        return completeOrFallback(promptFactory.forZodiac(zodiacResult), () -> fallbackInterpreter.generateZodiacAdvice(zodiacResult));
    }

    public String generateTojeongAdvice(TojeongResult tojeongResult) {
        return completeOrFallback(promptFactory.forTojeong(tojeongResult), () -> fallbackInterpreter.generateTojeongAdvice(tojeongResult));
    }

    private String completeOrFallback(AiPromptRequest request, FallbackSupplier fallback) {
        if (!properties.providerCallsEnabled() || provider.isEmpty()) {
            return fallback.get();
        }
        try {
            AiPromptResponse response = provider.get().complete(request);
            if (response == null || response.content() == null || response.content().isBlank()) {
                return fallback.get();
            }
            return response.content();
        } catch (Exception e) {
            log.warn("AI provider failed; using fallback: {}", e.getMessage());
            return fallback.get();
        }
    }

    @FunctionalInterface
    private interface FallbackSupplier {
        String get();
    }
}
