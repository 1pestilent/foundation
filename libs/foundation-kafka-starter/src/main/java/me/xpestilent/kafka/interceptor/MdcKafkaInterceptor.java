package me.xpestilent.kafka.interceptor;


import io.micrometer.tracing.BaggageInScope;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.MDC;
import org.springframework.kafka.listener.RecordInterceptor;

import java.nio.charset.StandardCharsets;

import static net.logstash.logback.argument.StructuredArguments.kv;


/**
 * Перехватчик для Kafka-консьюмеров, обеспечивающий трассировку.
 * Извлекает ({@code traceId}) из заголовков Kafka-сообщения
 * и помещает его в {@link MDC}.
 */
@Slf4j
@RequiredArgsConstructor
public class MdcKafkaInterceptor<K, V> implements RecordInterceptor<K, V> {

    private static final String TRACE_ID_HEADER = "traceId";

    private final Tracer tracer;

    private final ThreadLocal<BaggageInScope> baggageScope = new ThreadLocal<>();

    /**
     * Вызывается контейнером Spring Kafka перед тем, как передать сообщение в метод {@code @KafkaListener}.
     *
     * @param record   входящее сообщение из брокера
     * @param consumer Kafka-клиент
     * @return {@link ConsumerRecord} сообщение с наполненным {@link MDC}
     */
    @Override
    public ConsumerRecord<K, V> intercept(ConsumerRecord<K, V> record, org.apache.kafka.clients.consumer.Consumer<K, V> consumer) {
        var header = record.headers().lastHeader(TRACE_ID_HEADER);

        if (header != null && header.value() != null) {
            String traceId = new String(header.value(), StandardCharsets.UTF_8);

            MDC.put(TRACE_ID_HEADER, traceId);

            if (tracer != null) {
                try {
                    baggageScope.set(tracer.getBaggage(TRACE_ID_HEADER).makeCurrent(traceId));
                } catch (Exception e) {
                    log.warn("Failed to create Baggage", kv("traceId", traceId));
                }
            }
        }
        return record;
    }

    /**
     * Очищает {@link MDC} после успешной обработки сообщения.
     */
    @Override
    public void success(ConsumerRecord<K, V> record, org.apache.kafka.clients.consumer.Consumer<K, V> consumer) {
        clearContext();
    }

    /**
     * Очищает {@link MDC} после неудачной обработки сообщения.
     */
    @Override
    public void failure(ConsumerRecord<K, V> record, Exception exception, org.apache.kafka.clients.consumer.Consumer<K, V> consumer) {
        clearContext();
    }

    private void clearContext() {
        MDC.clear();
        BaggageInScope scope = baggageScope.get();
        if (scope != null) {
            scope.close();
            baggageScope.remove();
        }
    }
}