package com.fortune.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 텔레그램 테스트 요청 DTO
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-01-05
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TelegramTestRequest {

    @NotBlank(message = "메시지는 필수입니다")
    private String message;

    private Long chatId; // null이면 기본값 사용
} 