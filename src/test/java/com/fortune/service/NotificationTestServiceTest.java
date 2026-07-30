package com.fortune.service;

import com.fortune.dto.DailyFortuneResult;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class NotificationTestServiceTest {

    @Test
    void emailTestFailsWhenEmailDeliveryIsDisabled() {
        EmailService service = new EmailService(
                mock(JavaMailSender.class),
                new FortuneNotificationFormatter());
        ReflectionTestUtils.setField(service, "emailEnabled", false);

        assertThrows(IllegalStateException.class,
                () -> service.sendTestMessage("user@example.com", "테스트"));
        assertThrows(IllegalStateException.class,
                () -> service.sendDailyFortuneNow(
                        "user@example.com",
                        DailyFortuneResult.builder().date(LocalDate.now()).build(),
                        "사용자"));
    }

    @Test
    void telegramTestFailsWhenBotTokenIsMissing() {
        TelegramService service = new TelegramService();

        assertThrows(IllegalStateException.class,
                () -> service.sendTestMessage("테스트", null));
    }

    @Test
    void discordTestFailsWhenWebhookIsMissing() {
        DiscordService service = new DiscordService();

        assertThrows(IllegalStateException.class,
                () -> service.sendTestMessage("테스트", null));
    }
}
