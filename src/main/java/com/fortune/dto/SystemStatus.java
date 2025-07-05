package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 시스템 상태 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "시스템 상태")
public class SystemStatus {
    @Schema(description = "시스템명", example = "한국형 만세력 운세 시스템")
    private String systemName;

    @Schema(description = "버전", example = "2.5.0")
    private String version;

    @Schema(description = "상태", example = "RUNNING")
    private String status;

    @Schema(description = "현재 시간")
    private LocalDateTime currentTime;

    @Schema(description = "가동 시간", example = "24시간 30분 15초")
    private String uptime;

    @Schema(description = "기능 상태")
    private Map<String, Boolean> features;
}
