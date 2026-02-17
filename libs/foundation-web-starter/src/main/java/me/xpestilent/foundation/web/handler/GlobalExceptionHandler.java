package me.xpestilent.foundation.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.foundation.web.exception.BusinessException;
import me.xpestilent.foundation.web.response.ApiResponse;
import me.xpestilent.foundation.web.service.ExceptionLoggerService;
import me.xpestilent.foundation.web.service.MetricService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final Optional<MetricService> metricService;
    private final ExceptionLoggerService exceptionLoggerService;

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleAllExceptions(Exception exception) {

        exceptionLoggerService.log(exception);

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error("INTERNAL_SERVER_ERROR", exception.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {

        exceptionLoggerService.log(ex);

        ApiResponse<Void> response = ApiResponse.error(ex.getCode(), ex.getMessage());

        metricService.ifPresent(service -> service.enrich(response, ex, ex.getStatus()));

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler()
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));

        ApiResponse<Void> response = ApiResponse.error("VALIDATION_ERROR", "Ошибка валидации");
        response.getError().setDetails(errors);

        metricService.ifPresent(service -> service.enrich(response, ex, HttpStatus.BAD_REQUEST));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        exceptionLoggerService.log(ex);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiResponse<Void> response = ApiResponse.error("BAD_REQUEST", ex.getMessage());

        metricService.ifPresent(service -> service.enrich(response, ex, status));

        return ResponseEntity.status(status).body(response);
    }
}