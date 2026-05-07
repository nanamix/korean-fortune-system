package com.fortune.config;
import com.fortune.ai.AiFortuneProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
/**
 * 🤖 AI 서비스 설정 클래스 (안전한 버전)
 *
 * <p>AI 기능을 완전히 선택적으로 만들어 의존성 문제를 방지합니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Configuration
@EnableConfigurationProperties(AiFortuneProperties.class)
public class AIConfig {
    /**
     * 🔧 AI 서비스 상태 확인용 Bean
     * <p>AI 서비스가 활성화되었는지 여부에 따라 다른 메시지를 반환합니다.</p>
     * <p>이 Bean은 AI 서비스가 활성화된 경우에만 생성됩니다.</p>
     * <p>이 설정은 AI 서비스가 필요할 때만 로드되도록 하여, 의존성 문제를 방지합니다.</p>
     * <p>예를 들어, AI 서비스가 활성화된 경우에만 AI 관련 기능을 사용할 수 있습니다.</p>
     * <p>이 설정은 Spring Boot의 ConditionalOnProperty 어노테이션을 사용하여,
     * `app.fortune.ai.enabled` 프로퍼티가 `true`로 설정된 경우에만 활성화됩니다.</p>
     * <p>이렇게 함으로써, AI 서비스가 필요하지 않은 경우에는 관련 코드가 로드되지 않도록 하여,
     * 의존성 문제를 방지하고, 애플리케이션의 성능을 최적화합니다.</p>
     * @param 서비스 활성화 여부
     * @return 서비스 상태 메시지
     *
     */
    @Bean
    @ConditionalOnProperty(name = "app.fortune.ai.enabled", havingValue = "true")
    public String aiServiceStatus() {
        return "AI 서비스가 활성화되었습니다";
    }
    /**
     * 🚫 AI 서비스 비활성화 상태 Bean
     * <p>AI 서비스가 비활성화된 경우에 대한 메시지를 반환합니다.</p>
     * <p>이 Bean은 AI 서비스가 비활성화된 경우에만 생성됩니다.</p>
     * <p>이 설정은 AI 서비스가 필요하지 않은 경우에만 로드되도록 하여,
     * 의존성 문제를 방지합니다.</p>
     * @param 서비스 비활성화 여부
     * @return AI 서비스 비활성화 상태 메시지
     */
    @Bean
    @ConditionalOnProperty(name = "app.fortune.ai.enabled", havingValue = "false", matchIfMissing = true)
    public String aiServiceDisabled() {
        return "AI 서비스가 비활성화되었습니다";
    }
}
