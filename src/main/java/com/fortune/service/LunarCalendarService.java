package com.fortune.service;
import com.fortune.dto.LunarDate;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
/**
 * 음력 변환 서비스 — 천문 기반 {@link LunarSolarConverter}(Time4J KoreanCalendar) 위임.
 *
 * @author 하진영
 * @version 3.0.0
 * @since 2025-06-24
 */
@Service
public class LunarCalendarService {
    /**
     * 음력을 양력으로 변환 (평달 기준).
     * @param lunarYear 음력 년도
     * @param lunarMonth 음력 월
     * @param lunarDay 음력 일
     * @return 양력 날짜
     */
    public LocalDate convertLunarToSolar(int lunarYear, int lunarMonth, int lunarDay) {
        return LunarSolarConverter.lunarToSolar(lunarYear, lunarMonth, lunarDay, false);
    }

    /**
     * 음력을 양력으로 변환 (윤달 지정).
     */
    public LocalDate convertLunarToSolar(int lunarYear, int lunarMonth, int lunarDay, boolean leapMonth) {
        return LunarSolarConverter.lunarToSolar(lunarYear, lunarMonth, lunarDay, leapMonth);
    }

    /**
     * 양력을 음력으로 변환.
     * @param solarDate 양력 날짜
     * @return 음력 날짜
     */
    public LunarDate convertSolarToLunar(LocalDate solarDate) {
        LunarSolarConverter.LunarInfo info = LunarSolarConverter.solarToLunar(solarDate);
        // 음력 연도는 양력 연도 근사(정월 이전이면 -1). 월/일/윤달은 정확.
        int lunarYear = solarDate.getYear();
        if (solarDate.getMonthValue() == 1 || (solarDate.getMonthValue() == 2 && info.month() >= 11)) {
            lunarYear -= 1;
        }
        return LunarDate.builder()
                .year(lunarYear)
                .month(info.month())
                .day(info.day())
                .isLeapMonth(info.leapMonth())
                .build();
    }
}
