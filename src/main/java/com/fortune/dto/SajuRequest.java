package com.fortune.dto;

import lombok.Data;
import jakarta.validation.constraints.*;

/**
 * 사주 요청
 * 생년월일
 * 생시
 * 생분
 * 성별
 * 달력 구분
 */
@Data
public class SajuRequest {
    @NotNull
    @Min(1900)
    @Max(2100)
    private Integer birthYear; // 생년월일 - 필수 (1900-2100) 

    @NotNull
    @Min(1)
    @Max(12)
    private Integer birthMonth; // 생년월일 - 필수 (1-12)

    @NotNull
    @Min(1)
    @Max(31)
    private Integer birthDay; // 생년월일 - 필수 (1-31)

    @NotNull
    @Min(0)
    @Max(23)
    private Integer birthHour;  // 생시 - 필수 (0-23)

    @NotNull
    @Min(0)
    @Max(59)
    private Integer birthMinute; // 생분 - 필수 (0-59)

    @NotNull
    @Pattern(regexp = "M|F", message = "성별은 M(남성) 또는 F(여성)만 입력 가능합니다")
    private String gender; // 성별 - 필수 (M: 남성, F: 여성)

    @NotNull
    @Pattern(regexp = "SOLAR|LUNAR", message = "달력은 SOLAR(양력) 또는 LUNAR(음력)만 입력 가능합니다")
    private String calendarType; // 달력 구분 - 필수 (SOLAR: 양력, LUNAR: 음력)
}
