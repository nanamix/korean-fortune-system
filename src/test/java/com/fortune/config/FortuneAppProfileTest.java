package com.fortune.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

class FortuneAppProfileTest {

    @Test
    void separatesRequiredProfileFromOptionalNotifications() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("1. 공통 사용자 정보")
                .contains("2. 공통 알림 발송 정보")
                .contains(".profile-field-group .form-row { grid-template-columns:repeat(auto-fit,minmax(140px,1fr));")
                .contains("background: #fff; width: 100%; min-height:43px;")
                .contains("설정을 적용하는 것만으로는 발송되지 않습니다")
                .contains("현재 알림 미사용");
    }

    @Test
    void validatesSelectedNotificationChannelsWhenApplyingSettings() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("buildCommonProfile(true);")
                .contains("['email', 'both', 'all'].includes(type)")
                .contains("['telegram', 'both', 'all'].includes(type)")
                .contains("/^[0-9]+$/.test(telegramChatId)");
    }

    @Test
    void validatesCalendarDateAndSupportsLunarLeapMonth() throws IOException {
        String html = loadFortuneApp();

        assertThat(html)
                .contains("function updateCalendarFields()")
                .contains("실제 달력에 존재하는 출생일을 입력하세요.")
                .contains("출생일은 오늘 이후일 수 없습니다.")
                .contains("leapMonth:");
    }

    private String loadFortuneApp() throws IOException {
        return new ClassPathResource("static/fortune-app.html")
                .getContentAsString(StandardCharsets.UTF_8);
    }
}
