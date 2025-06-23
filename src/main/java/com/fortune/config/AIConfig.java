package com.fortune.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.openai.OpenAiChatOptions;

/**
 * 🤖 AI 서비스 설정 클래스
 * 
 * <p>OpenAI, Ollama 등 AI 서비스 연동을 위한 설정을 관리합니다.</p>
 * 
 * <h3>지원하는 AI 기능</h3>
 * <ul>
 *   <li>운세 해석 및 분석 강화</li>
 *   <li>개인화된 조언 생성</li>
 *   <li>자연어 처리 기반 질문 응답</li>
 *   <li>운세 트렌드 분석</li>
 *   <li>사용자 맞춤형 추천</li>
 * </ul>
 * 
 * @author AI팀
 * @version 2.0.0
 * @since 2025-06-23
 */
@Configuration
@ConditionalOnProperty(name = "app.fortune.ai.enabled", havingValue = "true", matchIfMissing = false)
public class AIConfig {

    @Value("${spring.ai.openai.api-key}")
    private String openAiApiKey;

    @Value("${app.fortune.ai.provider:openai}")
    private String aiProvider;

    /**
     * 🎯 OpenAI 채팅 모델 설정
     * 
     * <p>OpenAI GPT 모델을 사용한 채팅 서비스를 구성합니다.</p>
     * 
     * @return OpenAI 채팅 모델
     */
    @Bean
    @ConditionalOnProperty(name = "app.fortune.ai.provider", havingValue = "openai")
    public OpenAiChatModel openAiChatModel() {
        var openAiApi = new OpenAiApi(openAiApiKey);
        
        var options = OpenAiChatOptions.builder()
            .model("gpt-4") // 최신 GPT-4 모델 사용 (API 변경 반영)
            .temperature(0.7) // 창의성 조절 (0.0 ~ 1.0)
            .maxTokens(2000) // 최대 토큰 수
            .topP(0.9) // 확률 분포 조절
            .frequencyPenalty(0.0) // 반복 패널티
            .presencePenalty(0.0) // 존재 패널티
            .build();
        
        return new OpenAiChatModel(openAiApi, options);
    }

    /**
     * 💬 채팅 클라이언트 설정
     * 
     * <p>AI 모델과의 대화를 위한 클라이언트를 구성합니다.</p>
     * 
     * @param chatModel 채팅 모델
     * @return 채팅 클라이언트
     */
    @Bean
    public ChatClient chatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
            .defaultSystem("""
                당신은 한국 전통 사주학과 서양 점성술에 정통한 운세 전문가입니다.
                
                역할과 책임:
                • 사주팔자, 토정비결, 별자리 운세를 정확하고 친근하게 해석
                • 사용자의 상황에 맞는 실용적이고 긍정적인 조언 제공
                • 전통적인 지혜와 현대적인 관점을 조화롭게 결합
                • 미신적이거나 과도한 해석을 피하고 건전한 방향으로 안내
                
                응답 스타일:
                • 친근하고 따뜻한 어조 사용
                • 구체적이고 실용적인 조언 제공
                • 적절한 이모지와 한국어 존댓말 사용
                • 불안감을 조성하지 않는 건전한 해석
                
                금지사항:
                • 절대적인 미래 예언이나 단정적 표현 금지
                • 부정적이거나 두려움을 조성하는 해석 금지
                • 의학적, 법적, 재정적 조언 제공 금지
                • 개인 정보 요청이나 저장 금지
                """)
            .build();
    }
}