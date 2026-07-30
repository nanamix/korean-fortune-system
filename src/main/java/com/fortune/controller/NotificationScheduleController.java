package com.fortune.controller;

import com.fortune.dto.ApiResponse;
import com.fortune.dto.NotificationScheduleRequest;
import com.fortune.dto.NotificationScheduleResponse;
import com.fortune.service.NotificationScheduleService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/fortune/notification-schedules")
@RequiredArgsConstructor
public class NotificationScheduleController {
    private final NotificationScheduleService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NotificationScheduleResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.success(service.list()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NotificationScheduleResponse>> create(
            @Valid @RequestBody NotificationScheduleRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.create(request)));
    }

    @PatchMapping("/{id}/enabled")
    public ResponseEntity<ApiResponse<NotificationScheduleResponse>> setEnabled(
            @PathVariable Long id,
            @RequestBody EnabledRequest request) {
        return ResponseEntity.ok(ApiResponse.success(service.setEnabled(id, request.enabled())));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("예약이 삭제되었습니다."));
    }

    public record EnabledRequest(boolean enabled) {
    }
}
