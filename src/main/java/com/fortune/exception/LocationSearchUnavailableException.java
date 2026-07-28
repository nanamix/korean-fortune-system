package com.fortune.exception;

/**
 * 외부 위치 검색 제공자를 일시적으로 사용할 수 없을 때 발생한다.
 */
public class LocationSearchUnavailableException extends RuntimeException {

    public LocationSearchUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
