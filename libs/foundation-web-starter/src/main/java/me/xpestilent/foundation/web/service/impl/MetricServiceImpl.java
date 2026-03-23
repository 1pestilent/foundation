package me.xpestilent.foundation.web.service.impl;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import lombok.RequiredArgsConstructor;
import me.xpestilent.foundation.web.service.MetricService;
import org.springframework.http.HttpStatus;

import java.util.List;

@RequiredArgsConstructor
public class MetricServiceImpl implements MetricService {

    private final MeterRegistry meterRegistry;

    @Override
    public void recordError(Throwable exception, HttpStatus httpStatus) {
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
