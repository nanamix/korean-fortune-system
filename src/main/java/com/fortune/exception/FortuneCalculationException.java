package com.fortune.exception;

/**
 * 운세 계산 예외
 * 
 * @author 하진영
 * @version 2.5.0
 * @since 2025-06-24
 */
public class FortuneCalculationException extends RuntimeException {

    /**
     * 운세 계산 예외 생성자
     * 
     * @param message 예외 메시지
     */
    public FortuneCalculationException(String message) {
        super(message);
    }

    /**
     * 운세 계산 예외 생성자
     * 
     * @param message 예외 메시지
     * @param cause 예외 원인
     */
    public FortuneCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
