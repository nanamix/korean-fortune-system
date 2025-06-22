package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * 간지 달력 일자
 * 날짜
 * 일간 지지
 * 음력 소양
 * 길방위
 * 길한 색깔
 * 일일 운세
 * 좋은 날
 * 특이 사항
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GanjiCalendarDay {
    private LocalDate date;   // 날짜   
    private String dayPillar; // 일간 지지
    private String solarTerm; // 음력 소양
    private String luckyDirection; // 길방위
    private List<String> luckyColors; // 길한 색깔
    private String dailyLuck; // 일일 운세
    private boolean isGoodDay; // 좋은 날
    private String specialNote; // 특이 사항
}
