package me.xpestilent.foundation.web.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

@Getter
public class SystemException extends RuntimeException implements DetailedException {

    private final String code;
    private final HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
    private final Map<String, Object> details;

    public SystemException(String message, String code) {
        this(message, code, Collections.emptyMap());
    }

    public SystemException(String message, String code, Map<String, Object> details) {
        super(message);
        this.code = code;
        this.details = details != null ? details : Collections.emptyMap();
    }
}