package me.xpestilent.kafka.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.micrometer.tracing.Tracer;
import lombok.RequiredArgsConstructor;
import me.xpestilent.kafka.interceptor.MdcKafkaInterceptor;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.kafka.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.converter.StringJsonMessageConverter;
import org.springframework.messaging.converter.MessageConversionException;
import org.springframework.util.backoff.ExponentialBackOff;


/**
 * Глобальная автоконфигурация Kafka для императивного (блокирующего) стека.
 * <p>
 * Данная конфигурация активируется автоматически, если в приложении подключена зависимость {@code spring-kafka},
 * и отвечает за стандартизацию работы с брокером во всех микросервисах платформы.
 * <p>
 * Что настраивает этот класс:
 * <ul>
 * <li>Безопасную десериализацию JSON через изолированный {@link ObjectMapper}.</li>
 * <li>Политику повторных попыток (Exponential Backoff) при временных сбоях.</li>
 * <li>Автоматическую маршрутизацию безнадежных сообщений в DLT.</li>
 * <li>Подключение перехватчика {@link MdcKafkaInterceptor}.</li>
 * </ul>
 */
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(KafkaProperties.class)
public class KafkaAutoConfiguration {

    private final KafkaProperties properties;
    private final ObjectProvider<Tracer> tracerProvider;

    @Bean
    public MdcKafkaInterceptor<Object, Object> mdcKafkaInterceptor() {
        return new MdcKafkaInterceptor<>(tracerProvider.getIfAvailable());
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template,
            (record, ex) -> new TopicPartition(record.topic() + properties.getDltSuffix(), record.partition()));

        ExponentialBackOff backOff = new ExponentialBackOff(properties.getBackOffInitialInterval(), properties.getBackOffMultiplier());
        backOff.setMaxAttempts(properties.getMaxAttempts());

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(recoverer, backOff);

        errorHandler.addNotRetryableExceptions(
            IllegalArgumentException.class,
            ClassCastException.class,
            MessageConversionException.class
        );

        return errorHandler;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
        ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
        ConsumerFactory<Object, Object> kafkaConsumerFactory,
        DefaultErrorHandler errorHandler,
        MdcKafkaInterceptor<Object, Object> mdcKafkaInterceptor,
        ObjectMapper kafkaObjectMapper) {

        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory);

        factory.setRecordInterceptor(mdcKafkaInterceptor);

        factory.setCommonErrorHandler(errorHandler);

        factory.setRecordMessageConverter(new StringJsonMessageConverter(kafkaObjectMapper));

        return factory;
    }

}