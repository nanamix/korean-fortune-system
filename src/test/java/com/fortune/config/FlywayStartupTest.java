package com.fortune.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(properties = {
        "spring.flyway.enabled=true",
        "spring.flyway.locations=classpath:db/test-migration",
        "spring.mail.host=localhost"
})
@ActiveProfiles("test")
class FlywayStartupTest {

    @Autowired
    private Flyway flyway;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void runsFlywayMigrationBeforeHibernateStartup() {
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("1");
        assertThat(jdbcTemplate.queryForObject(
                "select count(*) from flyway_startup_probe", Integer.class)).isEqualTo(1);
    }
}
