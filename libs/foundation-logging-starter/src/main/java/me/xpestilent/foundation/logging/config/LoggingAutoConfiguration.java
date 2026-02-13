package me.xpestilent.foundation.logging.config;

import me.xpestilent.foundation.logging.filter.MdcHeaderFilter;
import me.xpestilent.foundation.logging.properties.LoggingProperties;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.web.filter.CommonsRequestLoggingFilter;

@AutoConfiguration
@EnableConfigurationProperties(LoggingProperties.class)
@ConditionalOnWebApplication
public class LoggingAutoConfiguration {

    @Bean
    public MdcHeaderFilter mdcHeaderFilter(LoggingProperties properties) {
        return new MdcHeaderFilter(properties);
    }

    @Bean
    public FilterRegistrationBean<MdcHeaderFilter> mdcHeaderFilterRegistration(MdcHeaderFilter filter) {
        FilterRegistrationBean<MdcHeaderFilter> registration = new FilterRegistrationBean<>(filter);
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        return registration;
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

