package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 음력 날짜
 * 년
 * 월
 * 일
 * 윤달 여부
 */
@Data
@Builder
public class LunarDate {
    private int year;   // 년
    private int month;  // 월
    private int day;    // 일
    private boolean isLeapMonth; // 윤달 여부

    /*
     * 음력 날짜 문자열 반환
     * 년
     * 월
     * 일
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
