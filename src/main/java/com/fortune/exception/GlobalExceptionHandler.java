package com.fortune.exception;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import com.fortune.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
/**
 * 글로벌 예외 처리기
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    /**
     * 운세 계산 예외 처리
     * <p>운세 계산 중 발생하는 예외를 처리합니다.</p>
     * <p>이 예외는 운세 계산 로직에서 발생할 수 있는 다양한 오류를 포괄합니다.</p>
     * <p>예를 들어, 잘못된 입력값이나 외부 API 호출 실패 등으로 인해 발생할 수 있습니다.</p>
     * <p>이 핸들러는 운세 계산 예외를 잡아 적절한 HTTP 응답을 반환합니다.</p>
     * <p>운세 계산 예외가 발생하면, 클라이언트에게 오류 메시지와 함께 400 Bad Request 상태 코드를 반환합니다.</p>
     * <p>이렇게 함으로써, 클라이언트는 운세 계산 중 발생한 문제를 이해하고 적절한 조치를 취할 수 있습니다.</p>
     * 
     * @param e 운세 계산 예외 <p>운세 계산 중 발생한 예외 객체입니다.</p>
     * @return ResponseEntity<ApiResponse<Void>> <p>운세 계산 예외에 대한 응답을 포함하는 ResponseEntity 객체입니다.</p>
     */
    @ExceptionHandler(FortuneCalculationException.class)
    public ResponseEntity<com.fortune.dto.ApiResponse<Void>> handleFortuneCalculationException(
            FortuneCalculationException e) {
        // 운세 계산 오류 로깅
        log.error("운세 계산 오류: {}", e.getMessage(), e);
        // 운세 계산 오류 응답 반환
        return ResponseEntity.badRequest()
                .body(com.fortune.dto.ApiResponse.error("운세 계산 중 오류가 발생했습니다: " + e.getMessage(), "운세 계산 오류"));
    }
    /**
     * 입력 검증 예외 처리
     * <p>클라이언트가 보낸 입력값이 유효하지 않을 때 발생하는 예외를 처리합니다.</p>
     * <p>예를 들어, 필수 필드가 누락되었거나 형식이 잘못된 경우에 발생합니다.</p>
     * <p>이 핸들러는 입력 검증 예외를 잡아 적절한 HTTP 응답을 반환합니다.</p>
     * <p>입력 검증 예외가 발생하면, 클라이언트에게 오류 메시지와 함께 400 Bad Request 상태 코드를 반환합니다.</p>
     * <p>이렇게 함으로써, 클라이언트는 잘못된 입력값을 수정하고 다시 요청할 수 있습니다.</p>
     *
     * @param e 입력 검증 예외 <p>입력값이 유효하지 않을 때 발생하는 예외 객체입니다.</p> <p>이 예외는 주로 @Valid 어노테이션을 사용한 입력 검증에서 발생합니다.</p>
     * @return ResponseEntity<ApiResponse<Void>> <p>입력 검증 예외에 대한 응답을 포함하는 ResponseEntity 객체입니다.</p>
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            MethodArgumentNotValidException e) {
        // 입력 검증 실패 메시지 생성
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse("입력값이 올바르지 않습니다.");
        // 입력 검증 실패 로깅
        log.warn("입력 검증 실패: {}", errorMessage);
        // 입력 검증 실패 응답 반환
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(errorMessage, "입력 검증 오류"));
    }
    /**
     * 정적 리소스 없음 예외 처리
     * <p>favicon.ico 등 정적 리소스가 없을 때 발생하는 예외를 처리합니다.</p>
     * <p>이 예외는 브라우저가 자동으로 요청하는 favicon 등이 없을 때 발생합니다.</p>
     * <p>이 핸들러는 정적 리소스 없음 예외를 잡아 적절한 HTTP 응답을 반환합니다.</p>
     * <p>정적 리소스 없음 예외가 발생하면, 클라이언트에게 404 Not Found 상태 코드를 반환합니다.</p>
     * <p>이렇게 함으로써, 클라이언트는 요청한 리소스가 없음을 인지할 수 있습니다.</p>
     * 
     * @param e 정적 리소스 없음 예외 <p>정적 리소스가 없을 때 발생하는 예외 객체입니다.</p>
     * @return ResponseEntity<ApiResponse<Void>> <p>정적 리소스 없음 예외에 대한 응답을 포함하는 ResponseEntity 객체입니다.</p>
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFoundException(
            org.springframework.web.servlet.resource.NoResourceFoundException e) {
        // favicon.ico 요청은 로그 레벨을 낮춤
        if (e.getMessage().contains("favicon.ico")) {
            log.debug("Favicon 요청: {}", e.getMessage());
        } else {
            log.warn("정적 리소스 없음: {}", e.getMessage());
        }
        // 정적 리소스 없음 응답 반환
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("요청한 리소스를 찾을 수 없습니다.", "리소스 없음"));
    }
    /**
     * 일반 예외 처리
     * <p>예상치 못한 오류가 발생했을 때 처리하는 핸들러입니다.</p>
     * <p>이 핸들러는 모든 종류의 예외를 포괄적으로 처리합니다.</p>
     * <p>예를 들어, 데이터베이스 연결 실패나 외부 API 호출 실패 등 다양한 오류가 발생할 수 있습니다.</p>
     * <p>이 핸들러는 예외를 잡아 적절한 HTTP 응답을 반환합니다.</p>
     * <p>예상치 못한 오류가 발생하면, 클라이언트에게 시스템 오류 메시지와 함께 500 Internal Server Error 상태 코드를 반환합니다.</p>
     * <p>이렇게 함으로써, 클라이언트는 시스템에서 문제가 발생했음을 인지하고, 나중에 다시 시도할 수 있습니다.</p>
     * @param e 예상치 못한 예외 <p>예상치 못한 오류가 발생했을 때의 예외 객체입니다.</p><p>이 예외는 일반적인 Exception 클래스를 상속받아 모든 종류의 예외를 처리합니다.</p> <p>예를 들어, NullPointerException, SQLException 등 다양한 예외가 포함될 수 있습니다.</p>
     * @return ResponseEntity<ApiResponse<Void>> <p>예상치 못한 오류에 대한 응답을 포함하는 ResponseEntity 객체입니다.</p>
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneralException(Exception e) {
        // 예상치 못한 오류 로깅
        log.error("예상치 못한 오류: {}", e.getMessage(), e);
        // 예상치 못한 오류 응답 반환
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("시스템 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", "시스템 오류"));
    }
    /**
     * 잘못된 날짜 예외 처리
     * <p>잘못된 날짜 예외를 처리합니다.</p>
     * <p>이 예외는 잘못된 날짜 형식이나 잘못된 날짜 범위 등으로 인해 발생할 수 있습니다.</p>
     * <p>이 핸들러는 잘못된 날짜 예외를 잡아 적절한 HTTP 응답을 반환합니다.</p>
     * <p>잘못된 날짜 예외가 발생하면, 클라이언트에게 오류 메시지와 함께 400 Bad Request 상태 코드를 반환합니다.</p>
     * <p>이렇게 함으로써, 클라이언트는 잘못된 날짜를 수정하고 다시 요청할 수 있습니다.</p>
     * 
     * @param e 잘못된 날짜 예외 <p>잘못된 날짜 예외 객체입니다.</p>
     * @return ResponseEntity<ApiResponse<Void>> <p>잘못된 날짜 예외에 대한 응답을 포함하는 ResponseEntity 객체입니다.</p>
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException e) {
        // 잘못된 인수 예외 로깅
        log.warn("잘못된 인수: {}", e.getMessage());
        // 잘못된 인수 예외 응답 반환
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("입력된 값이 올바르지 않습니다: " + e.getMessage(), "잘못된 인수"));
    }
}
