package com.fortune.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record NotificationScheduleResponse(
        Long id,
        boolean enabled,
        String recipientName,
        String notificationType,
        LocalTime scheduledTime,
        String timeZone,
        LocalDate lastRunDate,
        String lastStatus,
        String lastError,
        LocalDateTime lastRunAt,
        LocalDateTime createdAt) {
}
