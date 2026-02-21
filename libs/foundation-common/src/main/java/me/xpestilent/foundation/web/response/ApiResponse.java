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
    private T content;
    private String traceId;

    @Builder.Default
    private Instant timestamp = Instant.now();

    private ErrorDetails error;

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
        return ApiResponse.<T>builder().content(content).build();
    }

    public static ApiResponse<Void> error(String code, String message) {
        return ApiResponse.<Void>builder()
                .error(new ErrorDetails(code, message, null))
                .build();
    }
}
