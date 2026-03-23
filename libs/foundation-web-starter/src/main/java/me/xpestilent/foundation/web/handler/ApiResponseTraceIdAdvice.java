package me.xpestilent.foundation.web.handler;

import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import me.xpestilent.foundation.web.response.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Optional;

@RestControllerAdvice
@RequiredArgsConstructor
public class ApiResponseTraceIdAdvice implements ResponseBodyAdvice<Object> {

    private final Tracer tracer;

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return ApiResponse.class.isAssignableFrom(returnType.getParameterType());
    }


    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request, ServerHttpResponse response) {

        if (body instanceof ApiResponse<?> apiResponse) {
            if (apiResponse.getTraceId() == null && tracer != null) {
                String traceId = Optional.ofNullable(tracer.currentSpan())
                    .map(span -> span.context().traceId())
                    .orElse("n/a");

                apiResponse.setTraceId(traceId);
            }
        }
        return body;
    }
}
