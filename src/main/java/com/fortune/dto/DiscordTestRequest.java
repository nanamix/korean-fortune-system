package com.fortune.dto;

import lombok.Data;

/**
 * Discord 발송 테스트 요청 DTO.
 */
@Data
public class DiscordTestRequest {
    /** 전송할 메시지. */
    private String message;
    /** 대상 webhook URL (선택). 미지정 시 서버 기본 webhook 사용. */
    private String webhookUrl;
}
