package com.fortune.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 알림 발송 요청 DTO
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-01-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationRequest {

    @NotBlank(message = "수신자 이름은 필수입니다")
    private String recipientName;

    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;

    @Pattern(regexp = "^[0-9]+$", message = "텔레그램 채팅 ID는 숫자만 입력 가능합니다")
    private String telegramChatId;

    // Discord 공식 webhook URL만 허용(SSRF 방지). 미지정 시 서버 기본 webhook 사용.
    @Pattern(regexp = "^$|^https://(discord\\.com|discordapp\\.com|canary\\.discord\\.com|ptb\\.discord\\.com)(:\\d+)?/api/webhooks/.+$",
            message = "Discord webhook URL 형식이 올바르지 않습니다")
    private String discordWebhookUrl;

    @NotBlank(message = "발송 방법을 선택해주세요 (email, telegram, discord, both, all)")
    @Pattern(regexp = "^(email|telegram|discord|both|all)$",
            message = "발송 방법은 email, telegram, discord, both(email+telegram), all(전체) 중 하나여야 합니다")
    private String notificationType;
} 