package com.fortune;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;
// SpringDoc 어노테이션 제거 (호환성 문제로 인해)
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
 *   <li>📜 토정비결 64괘 운세</li>
 *   <li>⭐ 서양 별자리 운세</li>
 *   <li>📆 간지달력 및 길일 조회</li>
 *   <li>🤖 AI 기반 운세 분석</li>
 * </ul>
 *
 * <h3>기술 스택</h3>
 * <ul>
 *   <li>Java 17 (Eclipse Temurin)</li>
 *   <li>Spring Boot 3.3.x</li>
 *   <li>Spring Data JPA</li>
 *   <li>Spring Security</li>
 *   <li>Spring AI (OpenAI 연동)</li>
 *   <li>MySQL 8.0 / H2 Database</li>
 *   <li>Redis (캐싱)</li>
 *   <li>Docker & Docker Compose</li>
 * </ul>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@ConfigurationPropertiesScan
// OpenAPI 정의 제거 (SpringDoc 호환성 문제로 인해)
/**
 * 한국형 만세력 운세 시스템 메인 애플리케이션
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
public class KoreanFortuneApplication {
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
        /* Spring Boot 애플리케이션 시작 (KoreanFortuneApplication.class) */
        var app = new SpringApplication(KoreanFortuneApplication.class);
        // 배너 설정 (콘솔)
        app.setBannerMode(org.springframework.boot.Banner.Mode.CONSOLE);
        /* 애플리케이션 실행 */
        var context = app.run(args);
        /* 시작 완료 로그 */
        var environment = context.getEnvironment();
        /* 포트 설정 (8080) */
        var port = environment.getProperty("server.port", "8080");
        /* 프로필 설정 (환경 변수)
         * - 프로필 설정 (환경 변수)
         * ex) dev, prod 
         */
        var profile = String.join(",", environment.getActiveProfiles());
        /* 배너 출력 */
        System.out.println("""
            🔮 한국형 만세력 운세 시스템이 성공적으로 시작되었습니다!
            📊 서버 정보:
               • 포트: http://localhost:%s
               • 프로필: %s
               • API 문서: http://localhost:%s/swagger-ui.html
               • 헬스체크: http://localhost:%s/actuator/health
            ✨ 모든 준비가 완료되었습니다. 행운을 빕니다! ✨
            """.formatted(port, profile, port, port));
    }
}
