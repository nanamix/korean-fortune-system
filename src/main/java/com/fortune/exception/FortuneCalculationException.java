package com.fortune.exception;

/**
 * 운세 계산 예외
 * 메시지
 * 원인
 */
public class FortuneCalculationException extends RuntimeException {
    /**
     * 메시지
     * @param message 메시지
     */
    public FortuneCalculationException(String message) {
        super(message);
    }

    /**
     * 메시지
     * @param message 메시지
     */
    public FortuneCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
