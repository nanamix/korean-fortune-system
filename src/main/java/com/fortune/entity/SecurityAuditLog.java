package com.fortune.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
/**
 * 보안 감사 로그 엔티티
 * 원인: 보안 이벤트 기록 필요
 * 결과: action, user_id, timestamp, details 등 주요 정보 저장
 */
@Entity
@Table(name = "security_audit_log")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SecurityAuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /**
     * 사용자 ID (외래키)
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    /**
     * 수행된 작업
     */
    @Column(name = "action", nullable = false, length = 100)
    private String action;
    /**
     * 접근한 리소스
     */
    @Column(name = "resource", length = 100)
    private String resource;
    /**
     * 클라이언트 IP 주소
     */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    /**
     * User-Agent 정보
     */
    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;
    /**
     * 성공 여부
     */
    @Column(name = "success", nullable = false)
    private Boolean success;
    /**
     * 이벤트 발생 시각
     */
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    /**
     * 추가 상세 정보 (JSON 형태)
     */
    @Column(name = "details", columnDefinition = "JSON")
    private String details;
    /**
     * 생성 시 기본값 설정
     * - 이벤트 발생 시각이 없으면 현재 시간으로 설정
     * - 성공 여부가 없으면 false로 설정
     */
    @PrePersist
    public void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (success == null) {
            success = false;
        }
    }
} 