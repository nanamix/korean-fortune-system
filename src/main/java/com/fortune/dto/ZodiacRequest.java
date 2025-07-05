package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.time.LocalDate;


/**
 * 별자리 요청 DTO
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "별자리 운세 요청")
public class ZodiacRequest {

    @NotNull(message = "출생일자는 필수입니다")
    @Schema(description = "출생일자")
    private LocalDate birthDate;

    @NotNull(message = "대상일자는 필수입니다")
    @Schema(description = "운세를 보고자 하는 날짜")
    private LocalDate targetDate;
}
