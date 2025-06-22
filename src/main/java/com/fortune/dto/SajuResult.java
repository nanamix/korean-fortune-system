package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사주 결과 DTO
 * 년주
 * 월주
 * 일주
 * 시주
 * 일간 (본인 성향)
 * 실제 생일 (양력 변환 후)
 */
@Data
@Builder
public class SajuResult {
    private String yearPillar;          // 년주
    private String monthPillar;         // 월주
    private String dayPillar;           // 일주
    private String timePillar;          // 시주
    private String dayMaster;           // 일간 (본인 성향)
    private LocalDate birthDate;        // 실제 생일 (양력 변환 후)
    private LocalDateTime adjustedDateTime; // 태양시 보정 후 시간
    private String calendarType;        // SOLAR(양력) or LUNAR(음력)
    private String gender;              // M(남성) or F(여성)
    private String lunarBirthDate;      // 음력 생일 (음력인 경우만)

    public String getFormattedSaju() {
        return String.format("년주: %s, 월주: %s, 일주: %s, 시주: %s",
                yearPillar, monthPillar, dayPillar,
                timePillar != null ? timePillar : "미상");
    } // 년주, 월주, 일주, 시주 반환

    public String getGenderDescription() {
        return "M".equals(gender) ? "남성" : "여성";
    } // 성별 설명 반환

    public String getCalendarDescription() {
        return "SOLAR".equals(calendarType) ? "양력" : "음력";
    } // 달력 설명 반환
}
