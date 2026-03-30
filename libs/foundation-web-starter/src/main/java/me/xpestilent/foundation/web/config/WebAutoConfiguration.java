package me.xpestilent.foundation.web.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.tracing.Tracer;
import me.xpestilent.foundation.logging.service.ExceptionLoggerService;
import me.xpestilent.foundation.web.handler.ApiResponseTraceIdAdvice;
import me.xpestilent.foundation.web.handler.GlobalExceptionHandler;
import me.xpestilent.foundation.web.resolver.ClientInfoArgumentResolver;
import me.xpestilent.foundation.web.service.MetricService;
import me.xpestilent.foundation.web.service.impl.MetricServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;
import java.util.Optional;

@AutoConfiguration
public class WebAutoConfiguration implements WebMvcConfigurer {

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new ClientInfoArgumentResolver());
    }

    @Bean
    @ConditionalOnClass(Tracer.class)
    public MetricService metricService(ObjectProvider<MeterRegistry> meterRegistry) {

        return new MetricServiceImpl(meterRegistry.getIfAvailable());
    }

    @Bean
    @ConditionalOnMissingBean
    public ApiResponseTraceIdAdvice apiResponseTraceIdAdvice(Tracer tracer) {
        return new ApiResponseTraceIdAdvice(tracer);
    }

    @Bean
    public GlobalExceptionHandler globalExceptionHandler(
        Optional<MetricService> metricService,
        ExceptionLoggerService exceptionLoggerService
    ) {
        return new GlobalExceptionHandler(metricService, exceptionLoggerService);
    }
}