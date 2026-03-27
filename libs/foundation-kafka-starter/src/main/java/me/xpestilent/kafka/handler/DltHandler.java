package me.xpestilent.kafka.handler;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

import static net.logstash.logback.argument.StructuredArguments.kv;

/**
 * Обработчик битых сообщений.
 * <p>
 * Автоматически подписывается на топики с суффиксом {@code .dlt}.
 * Формирует логи для удобного парсинга в Kibana/Grafana.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DltHandler {

    private final ObjectProvider<Tracer> tracerProvider;

    @KafkaListener(
        topicPattern = ".*\\.dlt",
        groupId = "${spring.application.name}-dlt-group"
    )
    public void handleDltMessage(ConsumerRecord<String, String> record,
                                 @Header(value = KafkaHeaders.EXCEPTION_MESSAGE, required = false) byte[] exceptionMessage,
                                 @Header(value = KafkaHeaders.EXCEPTION_STACKTRACE, required = false) byte[] exceptionStacktrace) {

        String ex = exceptionMessage != null ? new String(exceptionMessage, StandardCharsets.UTF_8) : "Unknown error";

        Metrics.counter("kafka.dlt.messages",
            "topic", record.topic(),
            "reason", ex
        ).increment();

        Tracer tracer = tracerProvider.getIfAvailable();

        if (tracer != null && tracer.currentSpan() != null) {
            Span currentSpan = tracer.currentSpan();
            currentSpan.tag("error", "true");
            currentSpan.tag("kafka.dlt.reason", ex);
            currentSpan.event("SENT_TO_DLT");
        }

        log.error("CRITICAL: Message sent to DLT",
            kv("topic", record.topic()),
            kv("partition", record.partition()),
            kv("offset", record.offset()),
            kv("key", record.key()),
            kv("payload", record.value()),
            ex
        );

        if (exceptionStacktrace != null) {
            log.trace("DLT Exception Stacktrace", kv("ex", new String(exceptionStacktrace, StandardCharsets.UTF_8)));
        }
    }
}
