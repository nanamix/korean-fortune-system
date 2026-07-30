package com.fortune.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
    @Size(max = 254, message = "이메일은 254자 이하여야 합니다")
    private String email;

    @Pattern(regexp = "^-?[0-9]+$", message = "텔레그램 채팅 ID는 숫자 또는 음수 숫자만 입력 가능합니다")
    private String telegramChatId;

    // Discord 공식 webhook URL만 허용(SSRF 방지). 미지정 시 서버 기본 webhook 사용.
    @Pattern(regexp = "^$|^https://(discord\\.com|discordapp\\.com|canary\\.discord\\.com|ptb\\.discord\\.com)(:\\d+)?/api/webhooks/.+$",
            message = "Discord webhook URL 형식이 올바르지 않습니다")
    private String discordWebhookUrl;

    // OpenBao의 DISCORD_WEBHOOK_URL_<ALIAS>로 관리되는 영속 대상 이름.
    @Pattern(regexp = "^(default|[a-z0-9][a-z0-9-]{0,39})$",
            message = "Discord webhook 대상 이름 형식이 올바르지 않습니다")
    private String discordWebhookTarget;

    @NotBlank(message = "발송 방법을 선택해주세요 (email, telegram, discord, both, all)")
    @Pattern(regexp = "^(email|telegram|discord|both|all)$",
            message = "발송 방법은 email, telegram, discord, both(email+telegram), all(전체) 중 하나여야 합니다")
    private String notificationType;
}
