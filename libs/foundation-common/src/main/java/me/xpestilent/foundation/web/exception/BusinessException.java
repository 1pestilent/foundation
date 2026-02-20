package me.xpestilent.foundation.web.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

@Getter
public class BusinessException extends RuntimeException {
    private final String code;
    private final HttpStatus status;
    private final Map<String, Object> details;

    public BusinessException(String message, String code, HttpStatus status) {
        this(message, code, status, Collections.emptyMap());
    }

    public BusinessException(String message, String code, HttpStatus status, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.status = status;
        this.details = details != null ? details : Collections.emptyMap();
    }
}