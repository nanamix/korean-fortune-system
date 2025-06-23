package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class GanjiCalendarDay {
    private LocalDate date;                 // 날짜
    private String dayPillar;               // 일주 (예: 갑자, 을축 등)
    private String dayHeavenlyStem;         // 천간 (갑, 을, 병 등)
    private String dayBranch;               // 지지 (자, 축, 인 등)
    private String wuxingElement;           // 오행 (목, 화, 토, 금, 수)
    private String fortuneLevel;            // 길흉 수준 (대길/길/평/흉)
    private int fortuneScore;               // 길흉 점수 (0-100)
    private String luckyDirection;          // 길방위 (동쪽, 서쪽, 남쪽 등)
    private List<String> luckyColors;       // 길한 색깔 목록
    private String solarTerm;               // 절기 (있는 경우만, 예: 입춘, 춘분 등)
    private boolean isLuckyDay;             // 길일 여부
    private String dailyAdvice;             // 일일 조언
}
