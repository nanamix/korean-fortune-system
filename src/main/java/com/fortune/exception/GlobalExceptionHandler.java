package com.fortune.exception;

import com.fortune.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Map;

/*
 * 예외 처리 클래스
 * 입력값 검증 오류
 * 날짜 형식 오류
 * 파라미터 타입 오류
 * 운세 계산 오류
 * 기타 예외
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 입력값 검증 오류
     * 메시지
     * 원인
     * 메시지
     * @param ex 입력값 검증 오류
     * @return 입력값 검증 오류
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        // 입력값 검증 오류 메시지 생성
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return ResponseEntity.badRequest()
                .body(ApiResponse.error("입력값 검증 오류: " + errors.toString()));
    }

    /**
     * 날짜 형식 오류
     * 메시지
     * 원인
     * 메시지
     * @param ex 날짜 형식 오류
     * @return 날짜 형식 오류
     */
    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiResponse<String>> handleDateTimeParseException(DateTimeParseException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("날짜 형식이 올바르지 않습니다. YYYY-MM-DD 형식으로 입력해주세요."));
    }

    /**
     * 파라미터 타입 오류
     * 메시지
     * 원인
     * 메시지
     * @param ex 파라미터 타입 오류
     * @return 파라미터 타입 오류
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<String>> handleTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("파라미터 타입이 올바르지 않습니다: " + ex.getName()));
    }

    /**
     * 운세 계산 오류
     * 메시지
     * 원인
     * 메시지
     * @param ex 운세 계산 오류
     * @return 운세 계산 오류
     */
    @ExceptionHandler(FortuneCalculationException.class)
    public ResponseEntity<ApiResponse<String>> handleFortuneCalculationException(FortuneCalculationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("운세 계산 중 오류가 발생했습니다: " + ex.getMessage()));
    }

    /**
     * 기타 예외
     * 메시지
     * 원인
     * 메시지
     * @param ex 기타 예외
     * @return 기타 예외
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}
