package com.fortune;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
/**
 * 🔮 한국형 만세력 운세 시스템 메인 애플리케이션
 *
 * <p>이 애플리케이션은 전통 사주팔자, 토정비결, 별자리 운세 등을 제공하는
 * 종합 운세 서비스입니다.</p>
 *
 * <h3>주요 기능</h3>
 * <ul>
 *   <li>📊 사주팔자 계산 및 분석</li>
 *   <li>📅 일일/월별 운세 제공</li>
 *   <li>📜 토정비결 144괘 운세</li>
 *   <li>⭐ 서양 별자리 운세</li>
 *   <li>📆 간지달력 및 길일 조회</li>
 *   <li>📧 이메일 발송</li>
 *   <li>📱 텔레그램 발송</li>
 *   <li>🤖 AI 기반 운세 분석 (OpenAI)</li>
 *   <li>🔍 시스템 모니터링 (Spring Boot Actuator)</li>
 * </ul>
 *
 * <h3>기술 스택</h3>
 * <ul>
 *   <li>Java 25 (Amazon Corretto)</li>
 *   <li>Spring Boot 4.0.x</li>
 *   <li>Spring Data JPA</li>
 *   <li>Spring Security</li>
 *   <li>OpenAI-compatible AI 연동</li>
 *   <li>PostgreSQL / H2 Database</li>
 *   <li>Caffeine Cache</li>
 *   <li>Docker &amp; Docker Compose</li>
 * </ul>
 *
 * @author 하진영
 * @version 3.0.0-modernization
 * @since 2025-06-24
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@ConfigurationPropertiesScan
public class KoreanFortuneApplication {

    private static final Logger log = LoggerFactory.getLogger(KoreanFortuneApplication.class);

    /**
     * 메인 메서드
     *
     * @param args 인수
     */
    public static void main(String[] args) {
        /* JVM 최적화 설정 (헤드리스 모드) */
        System.setProperty("java.awt.headless", "true");
        /* 파일 인코딩 설정 (UTF-8) */
        System.setProperty("file.encoding", "UTF-8");
        /* 시간대 설정 (서울) */
        System.setProperty("user.timezone", "Asia/Seoul");
        /* Spring Boot 애플리케이션 시작 */
        var app = new SpringApplication(KoreanFortuneApplication.class);
        app.setBannerMode(org.springframework.boot.Banner.Mode.CONSOLE);
        /* 애플리케이션 실행 */
        var context = app.run(args);
        /* 시작 완료 로그 */
        var environment = context.getEnvironment();
        var port = environment.getProperty("server.port", "8080");
        var profile = String.join(",", environment.getActiveProfiles());
        log.info("""
            🔮 한국형 만세력 운세 시스템이 성공적으로 시작되었습니다!
            📊 서버 정보:
               • 포트: http://localhost:{}
               • 프로필: {}
               • API 문서: http://localhost:{}/api/docs
               • 헬스체크: http://localhost:{}/actuator/health
            ✨ 모든 준비가 완료되었습니다. 행운을 빕니다! ✨
            """, port, profile, port, port);
    }
}
