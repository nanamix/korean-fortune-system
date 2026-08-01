package com.fortune.config;

import static org.assertj.core.api.Assertions.assertThat;

import com.fortune.KoreanFortuneApplication;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
class SupabasePostgresMigrationTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @Test
    void migratesFreshPostgresSchemaThroughNotificationSchedule() throws Exception {
        Flyway flyway = Flyway.configure()
                .dataSource(POSTGRES.getJdbcUrl(), POSTGRES.getUsername(), POSTGRES.getPassword())
                .locations("classpath:db/migration/postgresql")
                .load();

        flyway.migrate();

        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("2");
        try (Connection connection = POSTGRES.createConnection("");
             Statement statement = connection.createStatement()) {
            assertThat(tableExists(statement, "users")).isTrue();
            assertThat(tableExists(statement, "security_audit_log")).isTrue();
            assertThat(tableExists(statement, "notification_schedule")).isTrue();
            assertThat(rowLevelSecurityEnabled(statement, "users")).isTrue();
            assertThat(rowLevelSecurityEnabled(statement, "notification_schedule")).isTrue();
        }

        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(
                KoreanFortuneApplication.class)
                .web(WebApplicationType.NONE)
                .run(
                        "--spring.profiles.active=supabase",
                        "--SUPABASE_DB_URL=" + POSTGRES.getJdbcUrl(),
                        "--SUPABASE_DB_USER=" + POSTGRES.getUsername(),
                        "--SUPABASE_DB_PASSWORD=" + POSTGRES.getPassword(),
                        "--spring.task.scheduling.enabled=false",
                        "--spring.mail.host=localhost")) {
            assertThat(context.containsBean("entityManagerFactory")).isTrue();
        }
    }

    private boolean tableExists(Statement statement, String tableName) throws Exception {
        try (ResultSet resultSet = statement.executeQuery("""
                SELECT EXISTS (
                    SELECT 1
                    FROM information_schema.tables
                    WHERE table_schema = 'public' AND table_name = '%s'
                )
                """.formatted(tableName))) {
            return resultSet.next() && resultSet.getBoolean(1);
        }
    }

    private boolean rowLevelSecurityEnabled(Statement statement, String tableName) throws Exception {
        try (ResultSet resultSet = statement.executeQuery("""
                SELECT relrowsecurity
                FROM pg_class
                WHERE oid = 'public.%s'::regclass
                """.formatted(tableName))) {
            return resultSet.next() && resultSet.getBoolean(1);
        }
    }
}
