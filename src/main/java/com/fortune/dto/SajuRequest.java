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

    /** 초 단위 절입 경계 검증용. 미지정 시 0초. */
    @Min(value = 0, message = "출생초는 0초 이상이어야 합니다")
    @Max(value = 59, message = "출생초는 59초 이하여야 합니다")
    private Integer birthSecond;

    @NotNull(message = "성별은 필수입니다")
    @Pattern(regexp = "^(M|F)$", message = "성별은 M(남성) 또는 F(여성)이어야 합니다")
    private String gender;

    @NotNull(message = "달력 타입은 필수입니다")
    @Pattern(regexp = "^(SOLAR|LUNAR)$", message = "달력 타입은 SOLAR(양력) 또는 LUNAR(음력)이어야 합니다")
    private String calendarType;

    /** 음력 윤달 여부 (LUNAR 입력에서만 의미). 미지정 시 평달. */
    private Boolean leapMonth;

    /**
     * 한국 내 출생지 경도. 미지정 시 기존 호환값인 동경 127.5도를 사용한다.
     * 표준시 자오선과의 차이는 경도 1도당 4분으로 보정한다.
     */
    @DecimalMin(value = "124.0", message = "출생지 경도는 동경 124도 이상이어야 합니다")
    @DecimalMax(value = "132.0", message = "출생지 경도는 동경 132도 이하여야 합니다")
    private Double birthLongitude;

    /** 균시차를 적용해 평균 태양시를 겉보기 태양시로 보정할지 여부. 기본값 false. */
    private Boolean applyEquationOfTime;

    /** Asia/Seoul tzdb의 역사적 서머타임을 표준시로 환산할지 여부. 기본값 true. */
    private Boolean applyHistoricalDst;

    // 알림 발송 관련 필드 (선택적)
    @Valid
    private NotificationRequest notification;
}
