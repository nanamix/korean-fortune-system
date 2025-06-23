package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 토정과 DTO
 * 토정과의 괘 정보를 담는 객체로, 괘 번호, 이름, 상징, 요약, 상세 운세, 점수, 길한 달, 주의할 달을 포함합니다.
 * @author 하진영
 * @version 1.0.0
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TojeongGwa {
    private int number;                 // 괘 번호 (1-64)
    private String name;                // 괘 이름
    private String symbol;              // 괘 상징
    private String summary;             // 요약
    private String detailedFortune;     // 상세 운세
    private int score;                  // 점수 (0-100)
    private List<Integer> luckyMonths;  // 길한 달
    private List<Integer> cautionMonths; // 주의할 달
}
