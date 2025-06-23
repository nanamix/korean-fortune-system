package com.fortune.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.fortune.dto.ApiResponse;
import com.fortune.dto.SystemStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@Tag(name = "시스템 API", description = "시스템 상태 및 정보 조회")
public class SystemController {

    @GetMapping("/status")
    @Operation(summary = "시스템 상태 확인", description = "시스템의 현재 상태를 확인합니다")
    public ResponseEntity<ApiResponse<SystemStatus>> getSystemStatus() {

        SystemStatus status = SystemStatus.builder()
                .systemName("한국형 만세력 운세 시스템")
                .version("1.0.0")
                .status("RUNNING")
                .currentTime(LocalDateTime.now())
                .uptime(getUptime())
                .features(getAvailableFeatures())
                .build();

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    private String getUptime() {
        long uptimeMillis = System.currentTimeMillis() -
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        long seconds = uptimeMillis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        return String.format("%d시간 %d분 %d초", hours, minutes % 60, seconds % 60);
    }

    private Map<String, Boolean> getAvailableFeatures() {
        Map<String, Boolean> features = new HashMap<>();
        features.put("사주팔자계산", true);
        features.put("일일운세", true);
        features.put("토정비결", true);
        features.put("별자리운세", true);
        features.put("간지달력", true);
        return features;
    }
}
