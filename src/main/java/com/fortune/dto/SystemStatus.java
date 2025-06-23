package com.fortune.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class SystemStatus {
    private String systemName;              // 시스템명
    private String version;                 // 버전
    private String status;                  // 상태
    private LocalDateTime currentTime;      // 현재 시간
    private String uptime;                  // 가동 시간
    private Map<String, Boolean> features;  // 기능 상태
}
