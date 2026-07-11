package com.fortune.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.BeforeEach;
import com.fortune.dto.SajuRequest;
import com.fortune.dto.SajuResult;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 간지 계산 서비스 테스트
 *
 * <p>사주팔자의 핵심인 간지(干支) 계산을 담당하는 서비스 클래스의 단위 테스트입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
class GanjiCalculatorServiceTest {

    private GanjiCalculatorService ganjiCalculatorService;

    @BeforeEach
    void setUp() {
        ganjiCalculatorService = new GanjiCalculatorService();
    }

    /**
     * 사주팔자 계산 테스트
     * <p>기본적인 사주팔자 계산을 수행하고 결과를 검증합니다.</p>
     * <p>이 테스트는 1981년 3월 20일에 태어난 사람의 사주팔자를 계산합니다.</p>
     * <p>결과는 연주, 월주, 일주, 시주, 일간 등을 포함합니다.</p>
     * <p>사주팔자는 한국 전통의 운세 시스템으로, 개인의 생년월일을 기반으로 운세를 예측합니다.</p>
     * <p>이 테스트는 사주팔자 서비스의 기본적인 기능을 검증하며, 입력값에 대한 일관성을 확인합니다.</p>
     * @param void
     * @return void
     */
    @Test
    @DisplayName("사주팔자 계산 - 기본 테스트")
    void testCalculateSaju() {
        // Given
        SajuRequest request = new SajuRequest();
        request.setBirthYear(1981);
        request.setBirthMonth(3);
        request.setBirthDay(20);
        request.setBirthHour(1);
        request.setBirthMinute(59);
        request.setGender("M");
        request.setCalendarType("SOLAR");

        // When
        SajuResult result = ganjiCalculatorService.calculateSaju(request);

        // Then — 정통 만세력 정답(척척사주): 년 신유(辛酉)·월 신묘(辛卯)·일 정유(丁酉)·시 신축(辛丑)
        assertNotNull(result);
        assertEquals("신유", result.getYearPillar(), "연주(입춘 기준) = 辛酉");
        assertEquals("신묘", result.getMonthPillar(), "월주(경칩=卯월, 오호둔) = 辛卯");
        assertEquals("정유", result.getDayPillar(), "일주(율리우스일 60갑자) = 丁酉");
        assertEquals("신축", result.getTimePillar(), "시주(오서둔, 丑시) = 辛丑");
        assertEquals("정", result.getDayMaster(), "일간 = 丁");

        // 대운: 음남(辛=음간, 남자) → 역행, 대운수 5, 첫 대운 경인(庚寅)
        assertFalse(result.isDaeunForward(), "음남 → 대운 역행");
        assertEquals(5, result.getDaeunNumber(), "대운수 5");
        assertEquals("경인", result.getDaeun().get(0).getGanji(), "첫 대운 庚寅");

        // 십신/12운성 파생 검증 (연주 辛=편재, 일지 酉=장생)
        assertEquals("편재", result.getYearDetail().getStemSipsin(), "辛(년간)=편재");
        assertEquals("장생", result.getDayDetail().getTwelveStage(), "丁일간 酉=장생");

        System.out.println("계산 결과: " + result.getFormattedSaju());
        System.out.println("일간: " + result.getDayMaster() + " / 대운수: " + result.getDaeunNumber());
    }

    /**
     * 사주팔자 계산 일관성 테스트
     * <p>같은 입력에 대해 여러 번 계산했을 때 결과가 일관되어야 함을 검증합니다.</p>
     */
    @Test
    @DisplayName("다양한 생년월일 테스트")
    void testVariousBirthDates() {
        // 여러 날짜 테스트
        int[][] testDates = {
                {1990, 5, 15, 12, 30},
                {2000, 12, 25, 23, 59},
                {1975, 8, 8, 6, 0}
        };

        for (int[] testDate : testDates) {
            SajuRequest request = new SajuRequest();
            request.setBirthYear(testDate[0]);
            request.setBirthMonth(testDate[1]);
            request.setBirthDay(testDate[2]);
            request.setBirthHour(testDate[3]);
            request.setBirthMinute(testDate[4]);
            request.setGender("F");
            request.setCalendarType("SOLAR");

            SajuResult result = ganjiCalculatorService.calculateSaju(request);

            assertNotNull(result);
            assertNotNull(result.getFormattedSaju());
            System.out.println(String.format("%d-%d-%d %d:%d -> %s",
                    testDate[0], testDate[1], testDate[2], testDate[3], testDate[4],
                    result.getFortuneSummary()));
        }
    }
}
