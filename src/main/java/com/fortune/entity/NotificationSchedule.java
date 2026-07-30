package com.fortune.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification_schedule")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationSchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_key", nullable = false, length = 128)
    private String ownerKey;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "recipient_name", nullable = false, length = 50)
    private String recipientName;

    @Column(length = 254)
    private String email;

    @Column(name = "telegram_chat_id", length = 32)
    private String telegramChatId;

    @Column(name = "discord_webhook_target", length = 40)
    private String discordWebhookTarget;

    @Column(name = "notification_type", nullable = false, length = 16)
    private String notificationType;

    @Column(name = "birth_year", nullable = false)
    private int birthYear;
    @Column(name = "birth_month", nullable = false)
    private int birthMonth;
    @Column(name = "birth_day", nullable = false)
    private int birthDay;
    @Column(name = "birth_hour", nullable = false)
    private int birthHour;
    @Column(name = "birth_minute", nullable = false)
    private int birthMinute;
    @Column(nullable = false, length = 1)
    private String gender;
    @Column(name = "calendar_type", nullable = false, length = 8)
    private String calendarType;
    @Column(name = "leap_month", nullable = false)
    private boolean leapMonth;
    @Column(name = "birth_longitude")
    private Double birthLongitude;
    @Column(name = "apply_equation_of_time", nullable = false)
    private boolean applyEquationOfTime;
    @Column(name = "apply_historical_dst", nullable = false)
    private boolean applyHistoricalDst;

    @Column(name = "scheduled_time", nullable = false)
    private LocalTime scheduledTime;

    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone;

    @Column(name = "last_run_date")
    private LocalDate lastRunDate;

    @Column(name = "last_status", length = 16)
    private String lastStatus;

    @Column(name = "last_error", length = 500)
    private String lastError;

    @Column(name = "last_run_at")
    private LocalDateTime lastRunAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
