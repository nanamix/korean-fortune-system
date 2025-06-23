package com.fortune.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.fortune.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * 운세 계산 예외 처리
     */
    @ExceptionHandler(FortuneCalculationException.class)
    public ResponseEntity<ApiResponse<Void>> handleFortuneCalculationException(
            FortuneCalculationException e) {
        log.error("운세 계산 오류: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("운세 계산 중 오류가 발생했습니다: " + e.getMessage()));
    }

    /**
     * 입력 검증 예외 처리
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {

        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");

        log.warn("입력 검증 실패: {}", errorMessage);
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errorMessage));
    }

    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        log.error("예상치 못한 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }

    /**
     * 잘못된 날짜 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e) {
        log.warn("잘못된 인수: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("입력된 값이 올바르지 않습니다: " + e.getMessage()));
    }
}
