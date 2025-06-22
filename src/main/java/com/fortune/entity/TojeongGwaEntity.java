package com.fortune.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 토정과 정보
 * 토정과 번호
 * 토정과 이름
 * 토정과 기호
 * 토정과 요약
 * 토정과 상세 운세
 */
@Entity
@Table(name = "tojeong_gwa")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TojeongGwaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 토정과 번호

    @Column(name = "gwa_number", unique = true, nullable = false)
    private Integer gwaNumber; // 토정과 번호

    @Column(name = "gwa_name", nullable = false, length = 10)
    private String gwaName; // 토정과 이름

    @Column(name = "gwa_symbol", nullable = false, length = 5)
    private String gwaSymbol; // 토정과 기호

    @Column(name = "summary", nullable = false)
    private String summary; // 토정과 요약

    @Column(name = "detailed_fortune", nullable = false, columnDefinition = "TEXT")
    private String detailedFortune; // 토정과 상세 운세

    @Column(name = "overall_score", nullable = false)
    private Integer overallScore = 50; // 토정과 점수
}
