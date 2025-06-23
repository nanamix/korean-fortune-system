package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import com.fortune.enums.Zodiac;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class ZodiacFortuneResult {
    private Zodiac zodiac;                      // 별자리 enum
    private String zodiacKoreanName;            // 별자리 한글명
    private LocalDate targetDate;               // 대상 날짜
    private ZodiacDailyFortune todayFortune;    // 오늘의 운세
    private ZodiacMonthlyFortune monthlyFortune; // 월별 운세
    private List<Zodiac> compatibleZodiacs;     // 궁합 좋은 별자리
    private List<Integer> luckyNumbers;         // 행운의 숫자
    private String luckyColor;                  // 행운의 색깔
    private String luckyStone;                  // 행운의 보석
    private String personality;                 // 성격 특성
}
