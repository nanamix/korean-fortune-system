package com.fortune.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Discord 발송 테스트 요청 DTO.
 */
@Data
public class DiscordTestRequest {
    /** 전송할 메시지. */
    @NotBlank(message = "메시지는 필수입니다")
    private String message;

    /** 대상 webhook URL (선택). 미지정 시 서버 기본 webhook 사용. Discord 공식 호스트만 허용(SSRF 방지). */
    @Pattern(regexp = "^$|^https://(discord\\.com|discordapp\\.com|canary\\.discord\\.com|ptb\\.discord\\.com)(:\\d+)?/api/webhooks/.+$",
            message = "Discord webhook URL 형식이 올바르지 않습니다")
    private String webhookUrl;

    /** OpenBao에서 관리되는 webhook 대상 이름. */
    @Pattern(regexp = "^(default|[a-z0-9][a-z0-9-]{0,39})$",
            message = "Discord webhook 대상 이름 형식이 올바르지 않습니다")
    private String webhookTarget;
}
