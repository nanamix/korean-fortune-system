package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import jakarta.validation.Valid;
import java.time.LocalDate;

/**
 * 별자리 요청 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ZodiacRequest {
    @NotNull(message = "출생일자는 필수입니다")
    private LocalDate birthDate;
    
    @NotNull(message = "대상일자는 필수입니다")
    private LocalDate targetDate;

    // 알림 발송 관련 필드 (선택적)
    @Valid
    private NotificationRequest notification;
}
