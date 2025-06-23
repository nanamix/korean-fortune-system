package com.fortune.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ZodiacRequest {
    @NotNull(message = "생년월일은 필수입니다")
    private LocalDate birthDate;

    @NotNull(message = "대상날짜는 필수입니다")
    private LocalDate targetDate;
}
