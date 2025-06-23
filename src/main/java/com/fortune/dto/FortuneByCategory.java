package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 분야별 운세 DTO
 * 종합 운세, 애정 운세, 재물 운세, 건강 운세, 직업 운세를 포함합니다.
 * 각 분야의 점수를 정수형으로 표현합니다.
 * @author 하진영
 * @version 1.0.0
 */
@Data
@Builder
public class FortuneByCategory {
    private int overall;    // 종합 운세
    private int love;       // 애정 운세
    private int money;      // 재물 운세
    private int health;     // 건강 운세
    private int career;     // 직업 운세
}
