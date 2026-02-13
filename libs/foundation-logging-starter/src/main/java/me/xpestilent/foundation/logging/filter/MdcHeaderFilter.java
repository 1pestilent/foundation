package me.xpestilent.foundation.logging.filter;


import lombok.RequiredArgsConstructor;
import me.xpestilent.foundation.logging.properties.LoggingProperties;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Фильтр парсит заголовки каждого HTTP-запроса и
 * наполняет MDC нужными полями из {@link LoggingProperties}
 *
 * @author Mikhail Ermakov
 * @since 16/01/2025
 */
@RequiredArgsConstructor
public class MdcHeaderFilter extends OncePerRequestFilter {
    private final LoggingProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            properties.getMdcHeaders().forEach(headerName -> {
                String value = request.getHeader(headerName);
                if (value != null) {
                    MDC.put(headerName, value);
                }
            });
            filterChain.doFilter(request, response);
        } finally {
            properties.getMdcHeaders().forEach(MDC::remove);
        }
    }
}