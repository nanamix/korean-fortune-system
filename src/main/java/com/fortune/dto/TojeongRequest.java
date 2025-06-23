package com.fortune.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 토정과 요청 DTO
 * 생년, 생월, 생일, 대상년도를 포함하며, 각 필드는 유효성 검사를 통해 올바른 값인지 확인합니다.
 * @author 하진영
 * @version 1.0.0
 */
@Data
public class TojeongRequest {
    @NotNull(message = "생년은 필수입니다")
    @Min(value = 1900, message = "생년은 1900년 이후여야 합니다")
    @Max(value = 2100, message = "생년은 2100년 이전이어야 합니다")
    private Integer birthYear;

    @NotNull(message = "생월은 필수입니다")
    @Min(value = 1, message = "생월은 1-12 사이여야 합니다")
    @Max(value = 12, message = "생월은 1-12 사이여야 합니다")
    private Integer birthMonth;

    @NotNull(message = "생일은 필수입니다")
    @Min(value = 1, message = "생일은 1-31 사이여야 합니다")
    @Max(value = 31, message = "생일은 1-31 사이여야 합니다")
    private Integer birthDay;

    @NotNull(message = "대상년도는 필수입니다")
    @Min(value = 2020, message = "대상년도는 2020년 이후여야 합니다")
    @Max(value = 2050, message = "대상년도는 2050년 이전이어야 합니다")
    private Integer targetYear;
}
