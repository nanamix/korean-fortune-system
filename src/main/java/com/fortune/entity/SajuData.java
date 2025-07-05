package com.fortune.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

/**
 * 사주 데이터 엔티티
 * 사용자의 사주 정보를 저장하는 엔티티
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Entity
@Table(name = "saju_data", indexes = {
    @Index(name = "idx_user_id", columnList = "user_id"),
    @Index(name = "idx_calculated_at", columnList = "calculated_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사주 데이터 엔티티")
public class SajuData {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "사주 데이터 ID")
    private Long id;

    @Schema(description = "사용자 정보")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "year_pillar", length = 2, nullable = false)
    @Schema(description = "년주 (2자리)", example = "甲")
    private String yearPillar;

    @Column(name = "month_pillar", length = 2, nullable = false)
    @Schema(description = "월주 (2자리)", example = "乙")
    private String monthPillar;

    @Column(name = "day_pillar", length = 2, nullable = false)
    @Schema(description = "일주 (2자리)", example = "丙")
    private String dayPillar;

    @Column(name = "time_pillar", length = 2)
    @Schema(description = "시주 (2자리)", example = "丁")
    private String timePillar;

    @Column(name = "day_master", length = 1, nullable = false)
    @Schema(description = "일간 (본인 성향)", example = "甲")
    private String dayMaster;

    @Column(name = "calculated_at", nullable = false)
    @Schema(description = "계산 일시")
    private LocalDateTime calculatedAt;

    /**
     * 사주 데이터 생성 시 계산 일시 설정  
     * - 계산 일시가 없으면 현재 시간으로 설정
     * - 계산 일시가 있으면 그대로 유지
     */
    @PrePersist
    protected void onCreate() {
        // 계산 일시가 없으면 현재 시간으로 설정
        if (calculatedAt == null) {
            calculatedAt = LocalDateTime.now();
        }
    }
}
