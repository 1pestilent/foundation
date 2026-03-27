package me.xpestilent.kafka.utils;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.nio.charset.StandardCharsets;

public class ReactiveTraceUtils {

    public static final String TRACE_ID_HEADER = "traceId";

    /**
     * Извлекает traceId из записи Kafka и помещает его в реактивный контекст
     */
    public static <T> Mono<T> withTraceContext(ConsumerRecord<?, ?> record, Mono<T> publisher) {
        var header = record.headers().lastHeader(TRACE_ID_HEADER);
        if (header != null && header.value() != null) {
            String traceId = new String(header.value(), StandardCharsets.UTF_8);
            return publisher.contextWrite(Context.of(TRACE_ID_HEADER, traceId));
        }
        return publisher;
    }
}
