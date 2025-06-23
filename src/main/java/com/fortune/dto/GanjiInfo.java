package com.fortune.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class GanjiInfo {
    private String dayPillar;           // 완전한 일주 (예: 갑자)
    private String heavenlyStem;        // 천간 (예: 갑)
    private String earthlyBranch;       // 지지 (예: 자)
    private int heavenlyStemIndex;      // 천간 인덱스 (0-9)
    private int earthlyBranchIndex;     // 지지 인덱스 (0-11)
}
