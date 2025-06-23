package com.fortune.exception;

/**
 * 운세 계산 예외
 * 메시지
 * 원인
 */
public class FortuneCalculationException extends RuntimeException {

    public FortuneCalculationException(String message) {
        super(message);
    }

    public FortuneCalculationException(String message, Throwable cause) {
        super(message, cause);
    }
}
