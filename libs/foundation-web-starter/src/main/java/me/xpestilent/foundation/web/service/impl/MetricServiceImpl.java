package me.xpestilent.foundation.web.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import me.xpestilent.foundation.web.response.ApiResponse;
import me.xpestilent.foundation.web.service.MetricService;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final Tracer tracer;
    private final MeterRegistry meterRegistry;

    @Override
    public void enrich(ApiResponse<?> response, Throwable exception, HttpStatus httpStatus) {
        if (tracer != null) {
            String traceId = Optional.ofNullable(tracer.currentSpan())
                .map(span -> span.context().traceId())
                .orElse("n/a");
            response.setTraceId(traceId);
        }

        if (meterRegistry != null) {
            List<Tag> tags = List.of(
                Tag.of("exception", exception.getClass().getSimpleName()),
                Tag.of("status", String.valueOf(httpStatus.value())),
                Tag.of("outcome", httpStatus.series().name())
            );
            meterRegistry.counter("http.server.errors.total", tags).increment();
        }
    }
}
