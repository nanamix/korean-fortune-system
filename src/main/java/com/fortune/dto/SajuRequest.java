package com.fortune.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사주팔자 계산 요청 DTO
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-01-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SajuRequest {

    @NotNull(message = "출생년도는 필수입니다")
    @Min(value = 1900, message = "출생년도는 1900년 이후여야 합니다")
    @Max(value = 2100, message = "출생년도는 2100년 이하여야 합니다")
    private Integer birthYear;

    @NotNull(message = "출생월은 필수입니다")
    @Min(value = 1, message = "출생월은 1월 이상이어야 합니다")
    @Max(value = 12, message = "출생월은 12월 이하여야 합니다")
    private Integer birthMonth;

    @NotNull(message = "출생일은 필수입니다")
    @Min(value = 1, message = "출생일은 1일 이상이어야 합니다")
    @Max(value = 31, message = "출생일은 31일 이하여야 합니다")
    private Integer birthDay;

    @NotNull(message = "출생시는 필수입니다")
    @Min(value = 0, message = "출생시는 0시 이상이어야 합니다")
    @Max(value = 23, message = "출생시는 23시 이하여야 합니다")
    private Integer birthHour;

    @NotNull(message = "출생분은 필수입니다")
    @Min(value = 0, message = "출생분은 0분 이상이어야 합니다")
    @Max(value = 59, message = "출생분은 59분 이하여야 합니다")
    private Integer birthMinute;

    @NotNull(message = "성별은 필수입니다")
    @Pattern(regexp = "^(M|F)$", message = "성별은 M(남성) 또는 F(여성)이어야 합니다")
    private String gender;

    @NotNull(message = "달력 타입은 필수입니다")
    @Pattern(regexp = "^(SOLAR|LUNAR)$", message = "달력 타입은 SOLAR(양력) 또는 LUNAR(음력)이어야 합니다")
    private String calendarType;

    // 알림 발송 관련 필드 (선택적)
    @Valid
    private NotificationRequest notification;
}
