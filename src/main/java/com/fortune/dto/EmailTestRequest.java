package com.fortune.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 이메일 알림 테스트 요청.
 */
@Data
public class EmailTestRequest {

    @NotBlank(message = "수신 이메일은 필수입니다")
    @Email(message = "올바른 이메일 형식이어야 합니다")
    private String email;

    @NotBlank(message = "메시지는 필수입니다")
    private String message;
}
