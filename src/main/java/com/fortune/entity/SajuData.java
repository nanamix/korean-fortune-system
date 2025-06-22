package com.fortune.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 사주 데이터 엔티티 (JPA)
 * 사주 데이터를 저장하는 엔티티
 * 사용자 정보와 사주 데이터를 함께 저장
 * 사주 데이터는 년주, 월주, 일주, 시주, 일간 성향으로 구성
 * 사주 데이터는 사용자 정보와 함께 저장
 */
@Entity
@Table(name = "saju_data")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SajuData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;    // 사주 데이터 ID (자동 생성)

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user; // 사용자 정보

    @Column(name = "year_pillar", length = 2, nullable = false)
    private String yearPillar; // 년주 (2자리) / 필수

    @Column(name = "month_pillar", length = 2, nullable = false)
    private String monthPillar; // 월주 (2자리) / 필수

    @Column(name = "day_pillar", length = 2, nullable = false)
    private String dayPillar; // 일주 (2자리) / 필수

    @Column(name = "time_pillar", length = 2)
    private String timePillar; // 시주 (2자리) / 선택

    @Column(name = "day_master", length = 1, nullable = false)
    private String dayMaster; // 일간 (본인 성향) / 필수

    @Column(name = "calculated_at")
    private LocalDateTime calculatedAt = LocalDateTime.now(); // 계산 일시 (기본값: 현재 시간)
}
