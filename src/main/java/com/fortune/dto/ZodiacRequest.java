package com.fortune.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 별자리 요청 DTO
 * 생년월일
 * 대상 날짜
 */
@Data
public class ZodiacRequest {
    @NotNull
    private LocalDate birthDate; // 생년월일 - 필수

    private LocalDate targetDate; // 대상 날짜 - 선택 (기본값: 오늘)
}
