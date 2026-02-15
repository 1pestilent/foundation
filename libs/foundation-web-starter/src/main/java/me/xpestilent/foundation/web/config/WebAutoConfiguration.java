package me.xpestilent.foundation.web.config;

import me.xpestilent.foundation.web.handler.GlobalExceptionHandler;
import me.xpestilent.foundation.web.service.ExceptionLoggerService;
import me.xpestilent.foundation.web.service.MetricService;
import me.xpestilent.foundation.web.service.impl.ExceptionLoggerServiceImpl;
import me.xpestilent.foundation.web.service.impl.MetricServiceImpl;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import java.util.Optional;

@AutoConfiguration
public class WebAutoConfiguration {

    @Bean
    public ExceptionLoggerService exceptionLoggerService() {
        return new ExceptionLoggerServiceImpl();
    }

    @Bean
    @ConditionalOnClass(Tracer.class)
    public MetricService metricService(ObjectProvider<Tracer> tracerProvider, ObjectProvider<MeterRegistry> meterRegistry) {

        return new MetricServiceImpl(tracerProvider.getIfAvailable(), meterRegistry.getIfAvailable());
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(
        Optional<MetricService> metricService,
        ExceptionLoggerService exceptionLoggerService) {
        return new GlobalExceptionHandler(metricService, exceptionLoggerService);
    }
}