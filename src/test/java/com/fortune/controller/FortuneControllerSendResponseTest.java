package com.fortune.controller;

import java.time.LocalDate;

import com.fortune.dto.ApiResponse;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import com.fortune.dto.TojeongRequest;
import com.fortune.dto.TojeongResult;
import com.fortune.dto.ZodiacFortuneResult;
import com.fortune.dto.ZodiacRequest;
import com.fortune.service.AIFortuneService;
import com.fortune.service.DailyFortuneService;
import com.fortune.service.DiscordService;
import com.fortune.service.EmailService;
import com.fortune.service.GanjiCalendarService;
import com.fortune.service.GanjiCalculatorService;
import com.fortune.service.SlackService;
import com.fortune.service.TelegramService;
import com.fortune.service.TojeongBigyeolService;
import com.fortune.service.ZodiacFortuneService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FortuneControllerSendResponseTest {

    private GanjiCalculatorService ganjiCalculatorService;
    private DailyFortuneService dailyFortuneService;
    private TojeongBigyeolService tojeongBigyeolService;
    private ZodiacFortuneService zodiacFortuneService;
    private FortuneController controller;

    @BeforeEach
    void setUp() {
        ganjiCalculatorService = mock(GanjiCalculatorService.class);
        dailyFortuneService = mock(DailyFortuneService.class);
        tojeongBigyeolService = mock(TojeongBigyeolService.class);
        zodiacFortuneService = mock(ZodiacFortuneService.class);
        controller = new FortuneController(
                ganjiCalculatorService,
                dailyFortuneService,
                tojeongBigyeolService,
                zodiacFortuneService,
                mock(GanjiCalendarService.class),
                mock(AIFortuneService.class),
                mock(EmailService.class),
                mock(TelegramService.class),
                mock(DiscordService.class),
                mock(SlackService.class));
    }

    @Test
    void returnsSajuResultAfterSendFlow() {
        SajuResult expected = SajuResult.builder().dayPillar("갑자").build();
        when(ganjiCalculatorService.calculateSaju(any())).thenReturn(expected);

        ResponseEntity<ApiResponse<SajuResult>> response =
                controller.calculateSajuAndSend(SajuRequest.builder().build());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(expected);
    }

    @Test
    void returnsDailyResultAfterSendFlow() {
        SajuResult saju = SajuResult.builder().dayPillar("갑자").build();
        DailyFortuneResult expected = DailyFortuneResult.builder().totalScore(80).build();
        when(ganjiCalculatorService.calculateSaju(any())).thenReturn(saju);
        when(dailyFortuneService.calculateDailyFortune(saju, LocalDate.now())).thenReturn(expected);

        ResponseEntity<ApiResponse<DailyFortuneResult>> response =
                controller.getTodayFortuneAndSend(SajuRequest.builder().build());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(expected);
    }

    @Test
    void returnsTojeongResultAfterSendFlow() {
        TojeongResult expected = TojeongResult.builder().gwaName("건위천").build();
        when(tojeongBigyeolService.calculateTojeong(any())).thenReturn(expected);

        ResponseEntity<ApiResponse<TojeongResult>> response =
                controller.calculateTojeongAndSend(TojeongRequest.builder().build());

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(expected);
    }

    @Test
    void returnsZodiacResultAfterSendFlow() {
        ZodiacRequest request = ZodiacRequest.builder()
                .birthDate(LocalDate.of(1981, 3, 20))
                .targetDate(LocalDate.now())
                .build();
        ZodiacFortuneResult expected = ZodiacFortuneResult.builder()
                .targetDate(request.getTargetDate())
                .build();
        when(zodiacFortuneService.calculateZodiacFortune(
                request.getBirthDate(), request.getTargetDate())).thenReturn(expected);

        ResponseEntity<ApiResponse<ZodiacFortuneResult>> response =
                controller.calculateZodiacFortuneAndSend(request);

        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getData()).isSameAs(expected);
    }
}
