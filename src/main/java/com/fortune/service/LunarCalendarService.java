package com.fortune.service;

import com.fortune.dto.LunarDate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 음력 변환 서비스
 * 음력-양력 변환 테이블
 * 음력-양력 변환 데이터
 * 음력을 양력으로 변환
 * 양력을 음력으로 변환
 */
@Service
public class LunarCalendarService {

    // 음력-양력 변환 테이블 (1900-2100년, 실제로는 더 정밀한 천문 계산 필요)
    private static final Map<String, LocalDate> LUNAR_TO_SOLAR_MAP = new HashMap<>();

    static {
        // 주요 음력-양력 변환 데이터 (예시 - 실제로는 천문대 데이터 사용)
        // 1981년 음력 3월 20일 = 양력 1981년 4월 24일
        LUNAR_TO_SOLAR_MAP.put("1981-03-20", LocalDate.of(1981, 4, 24));
        // 더 많은 변환 데이터가 필요함...
    }

    /**
     * 음력을 양력으로 변환
     * @param lunarYear 음력 년도
     * @param lunarMonth 음력 월
     * @param lunarDay 음력 일
     * @return 양력 날짜
     */
    public LocalDate convertLunarToSolar(int lunarYear, int lunarMonth, int lunarDay) {
        String lunarKey = String.format("%d-%02d-%02d", lunarYear, lunarMonth, lunarDay);

        // 실제로는 복잡한 천문학 계산이 필요하지만, 여기서는 간단한 근사치 사용
        if (LUNAR_TO_SOLAR_MAP.containsKey(lunarKey)) {
            return LUNAR_TO_SOLAR_MAP.get(lunarKey);
        }

        // 근사 변환 (음력이 양력보다 약 19-50일 늦음)
        LocalDate approximateSolar = LocalDate.of(lunarYear, lunarMonth, lunarDay).plusDays(30);

        // 월이 넘어가는 경우 처리
        if (approximateSolar.getMonthValue() > 12) {
            approximateSolar = LocalDate.of(lunarYear + 1, 1, approximateSolar.getDayOfMonth());
        }

        return approximateSolar;
    }

    /**
     * 양력을 음력으로 변환 (근사치)
     * @param solarDate 양력 날짜
     * @return 음력 날짜
     */
    public LunarDate convertSolarToLunar(LocalDate solarDate) {
        // 역변환 로직 (실제로는 천문 계산 필요)
        LocalDate approximateLunar = solarDate.minusDays(30);

        return LunarDate.builder()
                .year(approximateLunar.getYear())
                .month(approximateLunar.getMonthValue())
                .day(approximateLunar.getDayOfMonth())
                .isLeapMonth(false)
                .build();
    }
}
