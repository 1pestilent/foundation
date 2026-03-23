package me.xpestilent.foundation.web.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private int status;
    private T data;
    private ErrorDetails error;
    private String traceId;

    @Builder.Default
    private Instant timestamp = Instant.now();


    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ErrorDetails {
        private String code;
        private String message;
        private Object details;
    }

    public static <T> ApiResponse<T> success(T content) {
        return ApiResponse.<T>builder().data(content).build();
    }

    public static <T> ApiResponse<T> success(T content, int status) {
        return ApiResponse.<T>builder()
            .success(true)
            .status(201)
            .data(content)
            .build();
    }

    public static ApiResponse<Void> error(int status, String code, String message) {
        return ApiResponse.<Void>builder()
            .status(status)
            .success(false)
            .error(new ErrorDetails(code, message, null))
            .build();
    }

    public static ApiResponse<Void> error(int status, String code, String message, Object details) {
        return ApiResponse.<Void>builder()
            .status(status)
            .success(false)
            .error(new ErrorDetails(code, message, details))
            .build();
    }
}
