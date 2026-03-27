package me.xpestilent.kafka.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "foundation.kafka")
public class KafkaProperties {

    /**
     * Суффикс для топиков недоставленных сообщений
     */
    private String dltSuffix = ".dlt";

    /**
     * Максимальное количество попыток обработки сообщения перед отправкой в DLT
     */
    private int maxAttempts = 3;

    /**
     * Начальная задержка между попытками (в миллисекундах)
     */
    private long backOffInitialInterval = 1000L;

    /**
     * Множитель задержки (2.0 означает 1с -> 2с -> 4с)
     */
    private double backOffMultiplier = 2.0;
}