package me.xpestilent.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import reactor.kafka.receiver.ReceiverOptions;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
@ConditionalOnClass(reactor.kafka.receiver.KafkaReceiver.class)
@RequiredArgsConstructor
public class ReactiveKafkaAutoConfiguration {

    private final KafkaProperties springKafkaProperties;
    private final ObjectMapper kafkaObjectMapper;

    @Bean
    @ConditionalOnMissingBean(ReceiverOptions.class)
    public ReceiverOptions<String, Object> kafkaReceiverOptions() {
        log.info("Initializing Reactive Kafka Configuration...");

        Map<String, Object> props = new HashMap<>(springKafkaProperties.buildConsumerProperties(null));

        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        ReceiverOptions<String, Object> basicReceiverOptions = ReceiverOptions.create(props);

        return basicReceiverOptions
            .withValueDeserializer(new JsonDeserializer<>(Object.class, kafkaObjectMapper, false));
    }

    @Bean
    public ReactiveKafkaReceiverFactory reactiveKafkaReceiverFactory(ReceiverOptions<String, Object> options) {
        return new ReactiveKafkaReceiverFactory(options);
    }
}