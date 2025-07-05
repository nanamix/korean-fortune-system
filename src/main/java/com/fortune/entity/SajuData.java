package com.fortune.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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
public class SajuData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Column(name = "year_pillar", length = 2, nullable = false)
    private String yearPillar;
    @Column(name = "month_pillar", length = 2, nullable = false)
    private String monthPillar;
    @Column(name = "day_pillar", length = 2, nullable = false)
    private String dayPillar;
    @Column(name = "time_pillar", length = 2)
    private String timePillar;
    @Column(name = "day_master", length = 1, nullable = false)
    private String dayMaster;
    @Column(name = "calculated_at", nullable = false)
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
