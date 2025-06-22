package com.fortune.dto;

import com.fortune.enums.Zodiac;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

/**
 * 별자리 운세 결과
 * 별자리
 * 대상 날짜
 * 오늘 운세
 * 주간 운세
 * 월간 운세
 * 궁합 별자리
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacFortuneResult {
    private Zodiac zodiac;  // 별자리
    private LocalDate targetDate;  // 대상 날짜
    private ZodiacFortune todayFortune;  // 오늘 운세
    private ZodiacFortune weeklyFortune;  // 주간 운세
    private ZodiacFortune monthlyFortune;  // 월간 운세
    private List<Zodiac> compatibleZodiacs;  // 궁합 별자리
    private List<Integer> luckyNumbers;  // 행운의 숫자
    private String advice;  // 조언
    private String luckyDirection;  // 길방위
    private List<String> luckyColors;  // 길한 색깔
    private int totalScore;  // 전체 점수
}
