// ===== 23. 헬스체크 및 모니터링 =====

package com.fortune.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 운세 서비스 상태 헬스 체크
 * 데이터베이스 연결 상태
 * 운세 서비스 상태
 */
@Component
public class FortuneHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource; // 데이터베이스 연결 상태

    /**
     * 운세 서비스 상태 확인
     * @return 상태 정보
     */
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                        .withDetail("database", "Available")
                        .withDetail("fortune_service", "Running")
                        .build();
            } // 데이터베이스 연결 상태
        } catch (Exception e) {
            return Health.down()
                    .withDetail("database", "Unavailable")
                    .withDetail("error", e.getMessage())
                    .build();
        } // 운세 서비스 상태

        return Health.down().build(); // 운세 서비스 상태
    }
}
