package me.xpestilent.foundation.logging.filter;


import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.foundation.logging.properties.LoggingProperties;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Фильтр для обеспечения сквозной трассировки HTTP-запросов.
 * <p>
 * Он перехватывает каждый входящий запрос и выполняет следующие задачи:
 * <ol>
 * <li>Извлекает {@code traceId} из заголовков запроса или генерирует новый,
 * если запрос новый.</li>
 * <li>Помещает {@code traceId} в {@link MDC}
 * для локального структурированного логирования.</li>
 * <li>Создает {@link BaggageInScope} для автоматической передачи
 * контекста трассировки в исходящие сетевые вызовы (REST, Kafka).</li>
 * <li>Гарантирует безопасную очистку {@code ThreadLocal}после
 * завершения HTTP-запроса, предотвращая утечки памяти.</li>
 * </ol>
 *
 * @author Mikhail Ermakov
 * @since 16/01/2025
 */
@Slf4j
@RequiredArgsConstructor
public class MdcHeaderFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "traceId";
    private final LoggingProperties properties;

    private final Tracer tracer;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        HttpServletResponse response,
        FilterChain filterChain
    ) throws ServletException, IOException {

        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (!StringUtils.hasText(traceId)) {
            if (tracer != null) {
                traceId = tracer.nextSpan().context().traceId();
            } else {
                traceId = UUID.randomUUID().toString().replace("-", ""); // W3C формат
            }
        }

        MDC.put(TRACE_ID_HEADER, traceId);

        response.addHeader(TRACE_ID_HEADER, traceId);

        BaggageInScope baggageScope = null;
        if (tracer != null) {
            try {
                baggageScope = tracer.getBaggage(TRACE_ID_HEADER).makeCurrent(traceId);
            } catch (Exception e) {
                log.warn("Failed to create Baggage", e);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_HEADER);
            if (baggageScope != null) {
                baggageScope.close();
            }
        }
    }
}