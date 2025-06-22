package com.fortune.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@Tag(name = "시스템", description = "시스템 상태 및 헬스체크 API")
public class SystemController {

    @Operation(
            summary = "시스템 상태 조회",
            description = "API 서버의 현재 상태 정보를 조회합니다.",
            tags = {"시스템"}
    )
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSystemStatus() {
        Map<String, Object> status = new HashMap<>();
        status.put("service", "Korean Fortune System");
        status.put("version", "1.0.0");
        status.put("timestamp", LocalDateTime.now());
        status.put("status", "running");

        return ResponseEntity.ok(status);
    }
}
