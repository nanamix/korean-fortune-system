package com.fortune.ai;

import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(name = "app.fortune.ai.enabled", havingValue = "true")
public class OpenAiCompatibleFortuneProvider implements AiProviderPort {
    private static final ParameterizedTypeReference<Map<String, Object>> RESPONSE_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final AiFortuneProperties properties;

    public OpenAiCompatibleFortuneProvider(RestClient.Builder restClientBuilder, AiFortuneProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.baseUrl()).build();
        this.properties = properties;
    }

    @Override
    public AiPromptResponse complete(AiPromptRequest request) {
        Map<String, Object> response = restClient.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .headers(headers -> {
                    if (!properties.apiKey().isBlank()) {
                        headers.setBearerAuth(properties.apiKey());
                    }
                })
                .body(Map.of(
                        "model", request.model(),
                        "temperature", request.temperature(),
                        "messages", List.of(
                                Map.of("role", "system", "content", request.system()),
                                Map.of("role", "user", "content", request.user())
                        )
                ))
                .retrieve()
                .body(RESPONSE_TYPE);

        return new AiPromptResponse(extractContent(response), properties.provider(), false);
    }

    @SuppressWarnings("unchecked")
    private String extractContent(Map<String, Object> response) {
        if (response == null) {
            return "";
        }
        Object choicesValue = response.get("choices");
        if (!(choicesValue instanceof List<?> choices) || choices.isEmpty()) {
            return "";
        }
        Object firstChoice = choices.get(0);
        if (!(firstChoice instanceof Map<?, ?> choice)) {
            return "";
        }
        Object messageValue = choice.get("message");
        if (!(messageValue instanceof Map<?, ?> message)) {
            return "";
        }
        Object content = message.get("content");
        return content == null ? "" : content.toString();
    }
}
