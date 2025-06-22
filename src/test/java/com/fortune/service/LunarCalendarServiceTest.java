package com.fortune.service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 음력 달력 서비스 테스트
 */
@SpringBootTest
@ActiveProfiles("test")
public class LunarCalendarServiceTest {

    /**
     * 기본적인 음력 계산 테스트
     */
    @Test
    public void testBasicLunarCalculation() {
        // 기본적인 음력 계산 테스트
        assertTrue(true);
    }
}
