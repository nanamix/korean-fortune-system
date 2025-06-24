package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 사주팔자 계산 요청 DTO
 *
 * <p>사용자의 생년월일시와 성별, 달력 타입을 포함한 요청 데이터를 담는 클래스입니다.</p>
 *
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사주팔자 계산 요청 정보")
public class SajuRequest {

    @NotNull(message = "출생연도는 필수입니다")
    @Min(value = 1900, message = "출생연도는 1900년 이후여야 합니다")
    @Max(value = 2030, message = "출생연도는 2030년 이전이어야 합니다")
    @Schema(description = "출생연도", example = "1990", minimum = "1900", maximum = "2030")
    private int birthYear;

    @NotNull(message = "출생월은 필수입니다")
    @Min(value = 1, message = "출생월은 1월부터 12월까지입니다")
    @Max(value = 12, message = "출생월은 1월부터 12월까지입니다")
    @Schema(description = "출생월", example = "5", minimum = "1", maximum = "12")
    private int birthMonth;

    @NotNull(message = "출생일은 필수입니다")
    @Min(value = 1, message = "출생일은 1일부터 31일까지입니다")
    @Max(value = 31, message = "출생일은 1일부터 31일까지입니다")
    @Schema(description = "출생일", example = "15", minimum = "1", maximum = "31")
    private int birthDay;

    @NotNull(message = "출생시간은 필수입니다")
    @Min(value = 0, message = "출생시간은 0시부터 23시까지입니다")
    @Max(value = 23, message = "출생시간은 0시부터 23시까지입니다")
    @Schema(description = "출생시간", example = "14", minimum = "0", maximum = "23")
    private int birthHour;

    @NotNull(message = "출생분은 필수입니다")
    @Min(value = 0, message = "출생분은 0분부터 59분까지입니다")
    @Max(value = 59, message = "출생분은 0분부터 59분까지입니다")
    @Schema(description = "출생분", example = "30", minimum = "0", maximum = "59")
    private int birthMinute;

    @NotBlank(message = "성별은 필수입니다")
    @Pattern(regexp = "^[MF]$", message = "성별은 M(남성) 또는 F(여성)이어야 합니다")
    @Schema(description = "성별 (M: 남성, F: 여성)", example = "M", allowableValues = {"M", "F"})
    private String gender;

    @NotBlank(message = "달력 타입은 필수입니다")
    @Pattern(regexp = "^(SOLAR|LUNAR)$", message = "달력 타입은 SOLAR(양력) 또는 LUNAR(음력)이어야 합니다")
    @Schema(description = "달력 타입 (SOLAR: 양력, LUNAR: 음력)", example = "SOLAR", allowableValues = {"SOLAR", "LUNAR"})
    private String calendarType;
}
