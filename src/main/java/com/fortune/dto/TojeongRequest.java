package com.fortune.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
/**
 * 토정비결 요청 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TojeongRequest {
    @NotNull(message = "출생연도는 필수입니다")
    @Min(value = 1900, message = "출생연도는 1900년 이후여야 합니다")
    @Max(value = 2030, message = "출생연도는 2030년 이전이어야 합니다")
    private int birthYear;
    @NotNull(message = "출생월은 필수입니다")
    @Min(value = 1, message = "출생월은 1-12월이어야 합니다")
    @Max(value = 12, message = "출생월은 1-12월이어야 합니다")
    private int birthMonth;
    @NotNull(message = "출생일은 필수입니다")
    @Min(value = 1, message = "출생일은 1-31일이어야 합니다")
    @Max(value = 31, message = "출생일은 1-31일이어야 합니다")
    private int birthDay;
    @NotNull(message = "대상연도는 필수입니다")
    @Min(value = 2020, message = "대상연도는 2020년 이후여야 합니다")
    @Max(value = 2040, message = "대상연도는 2040년 이전이어야 합니다")
    private int targetYear;
}
