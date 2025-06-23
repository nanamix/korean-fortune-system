package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * API 응답 DTO
 * 성공 여부, 메시지, 데이터, 타임스탬프를 포함합니다.
 * @param <T> 응답 데이터 타입
 * @author 하진영
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;            // 성공 여부
    private String message;             // 응답 메시지
    private T data;                     // 응답 데이터
    private LocalDateTime timestamp;    // 응답 타임스탬프

    /**
     * 성공 응답 생성 메소드
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message("성공")
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * 실패 응답 생성 메소드
     * @param message 오류 메시지
     * @param <T> 데이터 타입
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
