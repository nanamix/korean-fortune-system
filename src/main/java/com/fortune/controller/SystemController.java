package com.fortune.controller;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.fortune.dto.ApiResponse;
import com.fortune.dto.SystemStatus;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
/**
 * 시스템 컨트롤러
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@RestController
@RequestMapping("/api/system")
public class SystemController {
    /**
     * 시스템 상태 확인
     * 
     * @return 시스템 상태
     */
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<SystemStatus>> getSystemStatus() {
        // 시스템 상태 생성
        SystemStatus status = SystemStatus.builder()
                .systemName("한국형 만세력 운세 시스템")
                .version("2.5.0")
                .status("RUNNING")
                .currentTime(LocalDateTime.now())
                .uptime(getUptime())
                .features(getAvailableFeatures())
                .build();
        return ResponseEntity.ok(ApiResponse.success(status));
    }
    /**
     * 시스템 운영 시간 계산 (초, 분, 시간)
     * 
     * @return 운영 시간
     */
    private String getUptime() {
        // 시스템 운영 시간 계산
        long uptimeMillis = System.currentTimeMillis() -
                java.lang.management.ManagementFactory.getRuntimeMXBean().getStartTime();
        // 초, 분, 시간 계산
        long seconds = uptimeMillis / 1000; // 초
        long minutes = seconds / 60; // 분
        long hours = minutes / 60; // 시간
        // 시간, 분, 초 포맷 반환
        return String.format("%d시간 %d분 %d초", hours, minutes % 60, seconds % 60);
    }
    /**
     * 사용 가능한 기능 목록 조회 (사주팔자계산, 일일운세, 토정비결, 별자리운세, 간지달력)
     * 
     * @return 사용 가능한 기능 목록
     */
    private Map<String, Boolean> getAvailableFeatures() {
        // 사용 가능한 기능 목록 생성
        Map<String, Boolean> features = new HashMap<>();
        // 사용 가능한 기능 목록 추가
        features.put("사주팔자계산", true);
        features.put("일일운세", true);
        features.put("토정비결", true);
        features.put("별자리운세", true);
        features.put("간지달력", true);
        return features;
    }
}
