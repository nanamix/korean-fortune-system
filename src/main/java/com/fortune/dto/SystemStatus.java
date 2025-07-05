package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;
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
public class SystemStatus {
    private String systemName;
    private String version;
    private String status;
    private LocalDateTime currentTime;
    private String uptime;
    private Map<String, Boolean> features;
}
