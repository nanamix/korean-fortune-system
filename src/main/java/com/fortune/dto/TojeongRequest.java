package com.fortune.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 토정과 요청 DTO
 * 생년월일
 * 대상 년도
 */
@Data
public class TojeongRequest {
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
    @Min(2024)
    @Max(2100)
    private Integer targetYear; // 대상 년도 - 필수 (2024-2100)
}
