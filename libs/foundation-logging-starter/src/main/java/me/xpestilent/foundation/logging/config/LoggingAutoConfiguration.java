package me.xpestilent.foundation.logging.config;

import io.micrometer.tracing.Tracer;
import me.xpestilent.foundation.logging.filter.MdcHeaderFilter;
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
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;


/**
 * Автоконфигурация модуля логирования и трассировки.
 * <p>
 * Что настраивает этот класс:
 * <ul>
 * <li>{@link MdcHeaderFilter} с наивысшим приоритетом. Перехватывает HTTP-запросы
 * раньше всех и добавляют туда данные для трассировки</li>
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

    @Bean
    @ConditionalOnMissingBean
    public ExceptionLoggerService exceptionLoggerService() {
        return new ExceptionLoggerServiceImpl();
    }
}