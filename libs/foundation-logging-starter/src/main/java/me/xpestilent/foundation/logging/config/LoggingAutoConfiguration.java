package me.xpestilent.foundation.logging.config;

import io.micrometer.tracing.Tracer;
import me.xpestilent.foundation.logging.filter.MdcHeaderFilter;
import me.xpestilent.foundation.logging.filter.ReactiveMdcHeaderFilter;
import me.xpestilent.foundation.logging.properties.LoggingProperties;
import me.xpestilent.foundation.logging.service.ExceptionLoggerService;
import me.xpestilent.foundation.logging.service.impl.ExceptionLoggerServiceImpl;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;
import reactor.core.publisher.Hooks;


/**
 * Автоконфигурация модуля логирования и трассировки.
 * <p>
 * <p>
 * Класс автоматически определяет тип веб-окружения (Servlet или Reactive)
 * и загружает соответствующие компоненты для логирования.
 * <p>
 * Что настраивает этот класс:
 * <ul>
 * <li><b>Для Servlet-приложений:</b> регистрирует {@link MdcHeaderFilter}
 * для работы с классическим ThreadLocal MDC.</li>
 * <li><b>Для WebFlux-приложений:</b> регистрирует {@link ReactiveMdcHeaderFilter}
 * и включает поддержку проброса Reactor Context.</li>
 * <li>{@link CommonsRequestLoggingFilter} детально логирует входящие HTTP-запросы
 * (тело, заголовки, параметры), если это разрешено в настройках.</li>
 * <li>{@link ExceptionLoggerService} занимается маршрутизации исключений по уровням логирования.</li>
 * </ul>
 */
@AutoConfiguration
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnWebApplication
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ExceptionLoggerService exceptionLoggerService() {
        return new ExceptionLoggerServiceImpl();
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    static class ServletLoggingConfiguration {

        @Bean
        public FilterRegistrationBean<MdcHeaderFilter> mdcHeaderFilter(
            LoggingProperties loggingProperties,
            ObjectProvider<Tracer> tracerProvider) {

            FilterRegistrationBean<MdcHeaderFilter> registrationBean = new FilterRegistrationBean<>();
            registrationBean.setFilter(new MdcHeaderFilter(
                loggingProperties,
                tracerProvider.getIfAvailable()
            ));
            registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);
            return registrationBean;
        }

        @Bean
        @ConditionalOnProperty(prefix = "foundation.logging", name = "log-requests", havingValue = "true", matchIfMissing = true)
        public CommonsRequestLoggingFilter commonsRequestLoggingFilter(LoggingProperties properties) {
            CommonsRequestLoggingFilter filter = new CommonsRequestLoggingFilter();
            filter.setIncludeQueryString(true);
            filter.setIncludePayload(true);
            filter.setMaxPayloadLength(properties.getMaxPayloadLength());
            filter.setIncludeHeaders(properties.isIncludeHeaders());
            filter.setAfterMessagePrefix("REQUEST DATA: ");
            return filter;
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    static class ReactiveLoggingConfiguration {

        @Bean
        public ReactiveMdcHeaderFilter reactiveMdcHeaderFilter() {
            Hooks.enableAutomaticContextPropagation();
            return new ReactiveMdcHeaderFilter();
        }
    }
}