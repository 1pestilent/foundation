package me.xpestilent.foundation.logging.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashSet;
import java.util.Set;

/**
 * Класс конфигурации для управления настройками логирования.
 * <p>
 * Параметры загружаются из файла {@code application.yaml} по префиксу {@code foundation.logging}.
 * @author Mikhail Ermakov
 * @since 16/01/2025
 */
@Data
@ConfigurationProperties(prefix = "foundation.logging")
public class LoggingProperties {

    /**
     * Включить логирование входящих HTTP-запросов (body, url, headers)
     */
    private boolean logRequests = true;

    /**
     * Нужно ли включать заголовки (headers) в лог самого запроса
     */
    private boolean includeHeaders = false;

    /**
     * Максимальная длина payload (body), чтобы не забить ELK (например, 10000 символов)
     */
    private int maxPayloadLength = 10000;

    /**
     * Список заголовков, значения которых нужно автоматически переложить в MDC.
     */
    private Set<String> mdcHeaders = new HashSet<>(Set.of(
        "X-User-Id",
        "X-Forwarded-For",
        "X-Request-ID"
    ));
}