package com.fortune.service;

import net.time4j.PlainDate;
import net.time4j.TemporalType;
import net.time4j.calendar.EastAsianMonth;
import net.time4j.calendar.EastAsianYear;
import net.time4j.calendar.KoreanCalendar;

import java.time.LocalDate;

/**
 * 음양력 변환기 — Time4J {@link KoreanCalendar}(천문 기반, KASI 정합) 위임.
 *
 * <p>기존의 {@code plusDays(30)} 근사를 대체한다. 정적 유틸이라
 * 무인자 생성자 기반 서비스/테스트에서도 그대로 쓸 수 있다.</p>
 */
public final class LunarSolarConverter {

    private LunarSolarConverter() {}

    /**
     * 음력 → 양력.
     *
     * @param lunarYear  음력 연도
     * @param lunarMonth 음력 월 (1~12)
     * @param lunarDay   음력 일
     * @param leapMonth  윤달 여부
     * @return 양력 날짜
     */
    public static LocalDate lunarToSolar(int lunarYear, int lunarMonth, int lunarDay, boolean leapMonth) {
        EastAsianMonth month = leapMonth
                ? EastAsianMonth.valueOf(lunarMonth).withLeap()
                : EastAsianMonth.valueOf(lunarMonth);
        EastAsianYear year = EastAsianYear.forGregorian(lunarYear);
        // ponytail: 소월(29일)/존재하지 않는 음력 30·31일 입력은 그 달 말일로 클램프.
        RuntimeException last = null;
        for (int d = Math.min(lunarDay, 30); d >= 1; d--) {
            try {
                KoreanCalendar kc = KoreanCalendar.of(year, month, d);
                return TemporalType.LOCAL_DATE.from(kc.transform(PlainDate.class));
            } catch (RuntimeException e) {
                last = e; // 그 달에 없는 일자 → 하루 줄여 재시도
            }
        }
        throw new IllegalArgumentException(
                "음력 날짜 변환 실패: %d-%d-%d(윤달=%b)".formatted(lunarYear, lunarMonth, lunarDay, leapMonth), last);
    }

    /** 양력 → 음력 (월/일/윤달). 연도는 양력 연도 근사로 별도 제공. */
    public static LunarInfo solarToLunar(LocalDate solar) {
        PlainDate p = TemporalType.LOCAL_DATE.translate(solar);
        KoreanCalendar kc = p.transform(KoreanCalendar.class);
        return new LunarInfo(
                kc.getMonth().getNumber(),
                kc.getDayOfMonth(),
                kc.getMonth().isLeap());
    }

    /** 양력→음력 결과(월/일/윤달). */
    public record LunarInfo(int month, int day, boolean leapMonth) {}
}
