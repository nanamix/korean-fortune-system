package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 분야별 운세
 * 종합운
 * 애정운
 * 재물운
 * 건강운
 * 직업운
 */
@Data
@Builder
public class FortuneByCategory {
    private FortuneCategory overall;    // 종합운
    private FortuneCategory love;       // 애정운
    private FortuneCategory money;      // 재물운
    private FortuneCategory health;     // 건강운
    private FortuneCategory work;       // 직업운
}
