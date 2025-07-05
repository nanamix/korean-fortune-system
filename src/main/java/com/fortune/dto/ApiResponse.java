package com.fortune.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import io.swagger.v3.oas.annotations.media.Schema;




/**
 *  API 응답 래퍼 DTO
 *  * <p>API 응답을 일관된 형식으로 감싸는 DTO입니다.</p>
 *  * <h3>주요 필드</h3>
 *  * <ul>
 *      <li><strong>success</strong>: 요청 성공 여부</li>
 *      <li><strong>data</strong>: 응답 데이터 (제네릭 타입)</li>
 *      <li><strong>message</strong>: 응답 메시지</li>
 *      <li><strong>errorCode</strong>: 오류 발생 시 오류 코드</li>
 *      <li><strong>timestamp</strong>: 응답 생성 시간</li>
 *      </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 응답 래퍼")
public class ApiResponse<T> {

    /**
     * 성공 여부
     */
    @Schema(description = "성공 여부")
    private boolean success;

    /**
     * 응답 데이터
     */
    @Schema(description = "응답 데이터")
    private T data;

    /**
     * 메시지
     */
    @Schema(description = "메시지")
    private String message;

    /**
     * 오류 코드
     */
    @Schema(description = "오류 코드")
    private String errorCode;

    /**
     * 타임스탬프
     */
    @Schema(description = "타임스탬프")
    private String timestamp;

    /**
     * 성공 응답 생성
     *
     * @param data 응답 데이터
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message("성공")
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }

    /**
     * 오류 응답 생성
     *
     * @param message 오류 메시지
     * @param errorCode 오류 코드
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(java.time.LocalDateTime.now().toString())
                .build();
    }
}
