package com.fortune.service;

import com.fortune.config.TestConfig;
import com.fortune.dto.DailyFortuneResult;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest
@Import(TestConfig.class)
class FortuneCachingIntegrationTest {

    @Autowired
    private GanjiCalculatorService ganjiCalculatorService;

    @Autowired
    private DailyFortuneService dailyFortuneService;

    @Autowired
    private CacheManager cacheManager;

    @BeforeEach
    void clearCaches() {
        cacheManager.getCache("saju-result").clear();
        cacheManager.getCache("daily-fortune").clear();
    }

    @Test
    void cachesFullSajuResultForSameInput() {
        SajuRequest request = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();

        SajuResult first = ganjiCalculatorService.calculateSaju(request);
        SajuResult second = ganjiCalculatorService.calculateSaju(request);

        assertSame(first, second);
    }

    @Test
    void cachesDailyFortuneForSameSajuAndDate() {
        SajuRequest request = SajuRequest.builder()
                .birthYear(1990)
                .birthMonth(5)
                .birthDay(15)
                .birthHour(14)
                .birthMinute(30)
                .gender("M")
                .calendarType("SOLAR")
                .build();
        SajuResult saju = ganjiCalculatorService.calculateSaju(request);
        LocalDate targetDate = LocalDate.of(2026, 9, 5);

        DailyFortuneResult first = dailyFortuneService.calculateDailyFortune(saju, targetDate);
        DailyFortuneResult second = dailyFortuneService.calculateDailyFortune(saju, targetDate);

        assertSame(first, second);
    }
}
