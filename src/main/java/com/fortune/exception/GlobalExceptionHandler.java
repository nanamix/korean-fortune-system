package com.fortune.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.fortune.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
/**
 * 글로벌 예외 처리기
 * 
 * @author 하진영
 * @version 2.6.0
 * @since 2025-06-24
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 운세 계산 예외 처리
     */
    @ExceptionHandler(FortuneCalculationException.class)
    public ResponseEntity<com.fortune.dto.ApiResponse<Void>> handleFortuneCalculationException(
            FortuneCalculationException e) {
        log.error("운세 계산 오류: {}", e.getMessage(), e);
        return ResponseEntity.badRequest()
                .body(com.fortune.dto.ApiResponse.error("운세 계산 중 오류가 발생했습니다: " + e.getMessage(), "운세 계산 오류"));
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
                .body(ApiResponse.error(errorMessage, "입력 검증 오류"));
    }
    /**
     * 잘못된 HTTP 메서드 예외 처리 (405 Method Not Allowed)
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodNotSupportedException(
            HttpRequestMethodNotSupportedException e) {
        log.warn("지원하지 않는 HTTP 메서드: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(ApiResponse.error("지원하지 않는 HTTP 메서드입니다: " + e.getMethod(), "메서드 오류"));
    }
    /**
     * JSON 파싱 오류 처리 (400 Bad Request)
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.warn("JSON 파싱 오류: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("요청 데이터 형식이 올바르지 않습니다.", "파싱 오류"));
    }
    /**
     * 정적 리소스 없음 예외 처리
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException e) {
        if (e.getMessage().contains("favicon.ico")) {
            log.debug("Favicon 요청: {}", e.getMessage());
        } else {
            log.warn("정적 리소스 없음: {}", e.getMessage());
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("요청한 리소스를 찾을 수 없습니다.", "리소스 없음"));
    }
    /**
     * 잘못된 인수 예외 처리
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e) {
        log.warn("잘못된 인수: {}", e.getMessage());
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("입력된 값이 올바르지 않습니다: " + e.getMessage(), "잘못된 인수"));
    }
    /**
     * 일반 예외 처리
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        log.error("예상치 못한 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "시스템 오류"));
    }
}
