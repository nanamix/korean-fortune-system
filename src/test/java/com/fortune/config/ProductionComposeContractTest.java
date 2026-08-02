package com.fortune.config;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProductionComposeContractTest {

    @Test
    void keepsSupabaseCutoverAndNotificationSchedulerPauseFailClosed() throws IOException {
        Path projectRoot = Path.of(System.getProperty("user.dir"));
        String productionCompose = Files.readString(
                projectRoot.resolve("docker/docker-compose.prod.yaml"));
        String openBaoOverride = Files.readString(
                projectRoot.resolve("docker/docker-compose.openbao.override.yml"));

        assertThat(productionCompose)
                .contains("SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod,supabase}")
                .contains("APP_FORTUNE_NOTIFICATION_SCHEDULE_CRON="
                        + "${APP_FORTUNE_NOTIFICATION_SCHEDULE_CRON:--}");
        assertThat(openBaoOverride)
                .contains("SUPABASE_DB_URL")
                .contains("SUPABASE_DB_USER")
                .contains("SUPABASE_DB_PASSWORD");
    }
}
