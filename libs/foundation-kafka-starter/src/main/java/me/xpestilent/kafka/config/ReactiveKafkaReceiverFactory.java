package me.xpestilent.kafka.config;

import lombok.RequiredArgsConstructor;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@RequiredArgsConstructor
public class ReactiveKafkaReceiverFactory {

    private final ReceiverOptions<String, Object> defaultOptions;

    /**
     * Создает готовый KafkaReceiver для указанного топика
     */
    public KafkaReceiver<String, Object> createReceiver(String topic) {
        ReceiverOptions<String, Object> options = defaultOptions.subscription(Collections.singleton(topic));
        return KafkaReceiver.create(options);
    }
}