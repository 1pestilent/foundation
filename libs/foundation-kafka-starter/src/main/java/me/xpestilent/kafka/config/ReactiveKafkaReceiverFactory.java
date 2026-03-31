package me.xpestilent.kafka.config;

import lombok.RequiredArgsConstructor;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.Collections;

@RequiredArgsConstructor
public class ReactiveKafkaReceiverFactory {

    private final ReceiverOptions<String, String> defaultOptions;

    /**
     * Создает готовый KafkaReceiver для указанного топика
     */
    public KafkaReceiver<String, String> createReceiver(String topic) {
        ReceiverOptions<String, String> options = defaultOptions.subscription(Collections.singleton(topic));
        return KafkaReceiver.create(options);
    }
}