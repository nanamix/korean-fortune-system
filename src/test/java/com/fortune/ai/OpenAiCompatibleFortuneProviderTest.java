package com.fortune.ai;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiCompatibleFortuneProviderTest {

    @Test
    void callsChatCompletionsAndExtractsMessageContent() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        AiFortuneProperties properties = new AiFortuneProperties(
                true,
                "openai",
                "gpt-5-mini",
                "https://api.openai.test/v1",
                "test-key",
                null,
                true
        );
        OpenAiCompatibleFortuneProvider provider = new OpenAiCompatibleFortuneProvider(builder, properties);

        server.expect(requestTo("https://api.openai.test/v1/chat/completions"))
                .andExpect(method(org.springframework.http.HttpMethod.POST))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess("""
                        {"choices":[{"message":{"content":"AI 해석 결과"}}]}
                        """, MediaType.APPLICATION_JSON));

        AiPromptResponse response = provider.complete(new AiPromptRequest(
                "gpt-5-mini",
                "system prompt",
                "user prompt",
                0.7
        ));

        assertThat(response.content()).isEqualTo("AI 해석 결과");
        assertThat(response.provider()).isEqualTo("openai");
        assertThat(response.fallback()).isFalse();
        server.verify();
    }
}
