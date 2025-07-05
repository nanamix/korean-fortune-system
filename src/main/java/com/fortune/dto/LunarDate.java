package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 음력 날짜 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LunarDate {
    private int year;
    private int month;
    private int day;
    private boolean isLeapMonth;
    /**
     * 음력 날짜 문자열 반환 (년 월 일)
     * 
     * @return 음력 날짜 문자열
     */
    @Override
    public String toString() {
        return String.format("%d년 %s%d월 %d일",
                year,
                isLeapMonth ? "윤" : "",
                month,
                day);
    }
}
