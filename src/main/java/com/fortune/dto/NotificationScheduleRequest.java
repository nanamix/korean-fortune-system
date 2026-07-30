package com.fortune.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationScheduleRequest {
    @Min(1900) @Max(2100) private int birthYear;
    @Min(1) @Max(12) private int birthMonth;
    @Min(1) @Max(31) private int birthDay;
    @Min(0) @Max(23) private int birthHour;
    @Min(0) @Max(59) private int birthMinute;
    @NotBlank @Pattern(regexp = "M|F") private String gender;
    @NotBlank @Pattern(regexp = "SOLAR|LUNAR") private String calendarType;
    private boolean leapMonth;
    @DecimalMin("124.0") @DecimalMax("132.0") private Double birthLongitude;
    private boolean applyEquationOfTime;
    @Builder.Default
    private boolean applyHistoricalDst = true;

    @NotNull private LocalTime scheduledTime;
    @NotBlank @Size(max = 64) private String timeZone;
    @NotNull @Valid private NotificationRequest notification;
}
