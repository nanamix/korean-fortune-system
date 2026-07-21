package com.fortune.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SlackTestRequest {
    @NotBlank private String message;
    @Pattern(regexp = "^$|^https://hooks\\.slack\\.com/services/.+$", message = "Slack webhook URL 형식이 올바르지 않습니다")
    private String webhookUrl;
}
