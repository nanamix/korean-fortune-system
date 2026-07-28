package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 별자리 요청 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacRequest {
    @NotNull(message = "출생일자는 필수입니다")
    private LocalDate birthDate;
    
    @NotNull(message = "대상일자는 필수입니다")
    private LocalDate targetDate;

    /**
     * 서양 점성술 개인화 입력. 기존 API 호환을 위해 모두 선택값이며,
     * 출생 시각·위치가 갖춰지면 상승궁까지 계산한다.
     */
    private LocalTime birthTime;

    @DecimalMin(value = "-90.0", message = "출생 위도는 -90~90 범위여야 합니다")
    @DecimalMax(value = "90.0", message = "출생 위도는 -90~90 범위여야 합니다")
    private Double birthLatitude;

    @DecimalMin(value = "-180.0", message = "출생 경도는 -180~180 범위여야 합니다")
    @DecimalMax(value = "180.0", message = "출생 경도는 -180~180 범위여야 합니다")
    private Double birthLongitude;

    @Size(max = 64, message = "시간대 이름은 64자 이하여야 합니다")
    private String timeZone;

    @Pattern(regexp = "SOLAR|LUNAR", message = "역법은 SOLAR 또는 LUNAR여야 합니다")
    private String calendarType;

    private Boolean leapMonth;

    // 알림 발송 관련 필드 (선택적)
    @Valid
    private NotificationRequest notification;
}
