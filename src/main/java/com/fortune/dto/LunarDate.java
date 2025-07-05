package com.fortune.dto;

import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "음력 날짜")
public class LunarDate {
    @Schema(description = "년")
    private int year;

    @Schema(description = "월")
    private int month;

    @Schema(description = "일")
    private int day;

    @Schema(description = "윤달 여부")
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
