package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 공통 API 응답 형식
 * 성공 응답 (성공 시 데이터 반환)
 * 실패 응답 (실패 시 메시지 반환)
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private boolean success;   // 성공 여부
    private String message;    // 메시지
    private T data;            // 데이터
    private String timestamp;  // 타임스탬프

    /*
     * 성공 응답 (성공 시 데이터 반환)
     * 성공 메시지
     * 데이터
     * 타임스탬프
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data, LocalDateTime.now().toString());
    }

    /*
     * 실패 응답 (실패 시 메시지 반환)
     * 실패 메시지
     * 데이터
     * 타임스탬프
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, LocalDateTime.now().toString());
    }
}
