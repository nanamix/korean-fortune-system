package com.fortune.ai;

import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuResult;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.ZodiacFortuneResult;
import java.util.Optional;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AiFortuneFacade {
    private final AiFortuneProperties properties;
    private final AiPromptFactory promptFactory;
    private final FallbackFortuneInterpreter fallbackInterpreter;
    private final Optional<AiProviderPort> provider;
    private volatile Attempt lastAttempt = Attempt.notAttempted();

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

    public String answerQuestion(SajuResult sajuResult, String question) {
        return completeOrFallback(promptFactory.forQuestion(sajuResult, question),
                () -> fallbackInterpreter.answerQuestion(sajuResult, question));
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

    public AiProviderStatus providerStatus() {
        boolean enabled = properties.providerCallsEnabled();
        boolean keyConfigured = !properties.apiKey().isBlank();
        String state = !enabled ? "FALLBACK_ONLY"
                : !keyConfigured ? "NOT_CONFIGURED"
                : lastAttempt.state();
        String reasonCode = !enabled ? "PROVIDER_DISABLED"
                : !keyConfigured ? "API_KEY_MISSING"
                : lastAttempt.reasonCode();
        String reason = !enabled ? "외부 AI 호출이 비활성화되어 규칙 기반 해석을 사용합니다."
                : !keyConfigured ? "API 키가 설정되지 않았습니다."
                : lastAttempt.reason();
        return new AiProviderStatus(
                state,
                properties.provider(),
                properties.model(),
                enabled,
                keyConfigured,
                properties.fallbackEnabled(),
                reasonCode,
                reason,
                lastAttempt.at(),
                compatibleProviders());
    }

    private String completeOrFallback(AiPromptRequest request, FallbackSupplier fallback) {
        if (!properties.providerCallsEnabled() || provider.isEmpty()) {
            return fallback.get();
        }
        try {
            AiPromptResponse response = provider.get().complete(request);
            if (response == null || response.content() == null || response.content().isBlank()) {
                lastAttempt = Attempt.failure("EMPTY_RESPONSE", "외부 AI가 빈 응답을 반환했습니다.");
                return fallback.get();
            }
            lastAttempt = Attempt.success();
            return response.content();
        } catch (Exception e) {
            lastAttempt = classifyFailure(e);
            log.warn("AI provider failed; using fallback: {}", e.getMessage());
            return fallback.get();
        }
    }

    private Attempt classifyFailure(Exception exception) {
        String message = Optional.ofNullable(exception.getMessage()).orElse("");
        if (message.contains("insufficient_quota")) {
            return Attempt.failure("INSUFFICIENT_QUOTA", "API 크레딧 또는 결제 한도가 부족합니다.");
        }
        if (message.startsWith("429") || message.contains("Too Many Requests")) {
            return Attempt.failure("RATE_LIMITED", "요청 또는 토큰 사용 한도를 초과했습니다.");
        }
        if (message.startsWith("401")) {
            return Attempt.failure("INVALID_API_KEY", "API 키가 올바르지 않거나 만료되었습니다.");
        }
        if (message.startsWith("403")) {
            return Attempt.failure("MODEL_FORBIDDEN", "계정 또는 프로젝트에 모델 사용 권한이 없습니다.");
        }
        if (message.startsWith("400") || message.startsWith("404")) {
            return Attempt.failure("MODEL_OR_ENDPOINT_INVALID", "모델 ID 또는 API 주소를 확인해야 합니다.");
        }
        return Attempt.failure("PROVIDER_UNAVAILABLE", "외부 AI가 응답하지 않아 규칙 기반 해석으로 전환했습니다.");
    }

    private List<AiProviderStatus.ProviderOption> compatibleProviders() {
        return List.of(
                option("openai", "OpenAI", "직접 연결", "gpt-5.4-mini", "gpt-5-mini", "gpt-4.1-mini"),
                option("deepseek", "DeepSeek", "설정 교체", "deepseek-v4-flash", "deepseek-v4-pro"),
                option("gemini", "Google Gemini", "OpenAI 호환", "gemini-3.5-flash", "gemini-flash-latest"),
                option("groq", "GroqCloud", "OpenAI 호환", "openai/gpt-oss-20b", "openai/gpt-oss-120b", "llama-3.3-70b-versatile"),
                option("mistral", "Mistral AI", "OpenAI 호환", "mistral-small-latest", "mistral-large-latest"),
                option("anthropic", "Anthropic", "OpenAI 호환 프록시", "claude-sonnet-4"),
                option("ollama", "Ollama", "사설망·로컬", "llama3.3", "qwen2.5"),
                option("openrouter", "OpenRouter", "멀티 제공자 게이트웨이", "openai/*", "anthropic/*", "google/*", "deepseek/*"));
    }

    private AiProviderStatus.ProviderOption option(String id, String name, String connection, String... models) {
        return new AiProviderStatus.ProviderOption(id, name, connection, List.of(models));
    }

    private record Attempt(String state, String reasonCode, String reason, Instant at) {
        private static Attempt notAttempted() {
            return new Attempt("CONFIGURED", "NOT_TESTED", "설정은 완료됐지만 현재 프로세스에서 아직 외부 요청을 확인하지 않았습니다.", null);
        }

        private static Attempt success() {
            return new Attempt("AVAILABLE", "OK", "외부 AI가 정상 응답했습니다.", Instant.now());
        }

        private static Attempt failure(String code, String reason) {
            return new Attempt("UNAVAILABLE", code, reason, Instant.now());
        }
    }

    @FunctionalInterface
    private interface FallbackSupplier {
        String get();
    }
}
