package me.xpestilent.kafka.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnClass(reactor.kafka.receiver.KafkaReceiver.class)
@RequiredArgsConstructor
public class ReactiveKafkaAutoConfiguration {

    private final KafkaProperties springKafkaProperties;

    @Bean
    @ConditionalOnMissingBean(ReceiverOptions.class)
    public ReceiverOptions<String, String> kafkaReceiverOptions() {
        log.info("Initializing Reactive Kafka Configuration (String-based)...");

        Map<String, Object> props = new HashMap<>(springKafkaProperties.buildConsumerProperties(null));

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);

        return ReceiverOptions.create(props);
    }

    @Bean
    public ReactiveKafkaReceiverFactory reactiveKafkaReceiverFactory(ReceiverOptions<String, String> options) {
        return new ReactiveKafkaReceiverFactory(options);
    }
}