package me.xpestilent.foundation.web.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.foundation.logging.service.ExceptionLoggerService;
import me.xpestilent.foundation.web.exception.BusinessException;
import me.xpestilent.foundation.web.response.ApiResponse;
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
        HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ApiResponse.error(status.value(), status.getReasonPhrase(), exception.getMessage()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessException(BusinessException ex) {

        exceptionLoggerService.log(ex);

        ApiResponse<Void> response = ApiResponse.error(ex.getStatus().value(), ex.getCode(), ex.getMessage());

        if (ex.getDetails() != null && !ex.getDetails().isEmpty()) {
            response.getError().setDetails(ex.getDetails());
        }

        metricService.ifPresent(service -> service.recordError(ex, ex.getStatus()));

        return ResponseEntity.status(ex.getStatus()).body(response);
    }

    @ExceptionHandler()
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
            errors.put(error.getField(), error.getDefaultMessage()));

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiResponse<Void> response = ApiResponse.error(status.value(), status.getReasonPhrase(), "Ошибка валидации");
        response.getError().setDetails(errors);

        metricService.ifPresent(service -> service.recordError(ex, status));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadRequest(IllegalArgumentException ex) {
        exceptionLoggerService.log(ex);

        HttpStatus status = HttpStatus.BAD_REQUEST;
        ApiResponse<Void> response = ApiResponse.error(status.value(), status.getReasonPhrase(), ex.getMessage());

        metricService.ifPresent(service -> service.recordError(ex, status));

        return ResponseEntity.status(status).body(response);
    }
}