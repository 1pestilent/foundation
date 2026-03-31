package me.xpestilent.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.kafka.config.ReactiveKafkaReceiverFactory;
import me.xpestilent.notification.event.UserRegisteredEvent;
import me.xpestilent.notification.service.EmailService;
import me.xpestilent.notification.service.NotificationUserService;
import me.xpestilent.notification.service.TemplateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.ReceiverRecord;

import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;


@Slf4j
@Component
@RequiredArgsConstructor
public class UserRegistrationListener {

    private final ReactiveKafkaReceiverFactory receiverFactory;
    private final NotificationUserService userService;
    private final TemplateService templateService;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;

    @Value("${topics.user}")
    private String userTopic;

    @Value("${app.auth.verify-url}")
    private String verifyUrlPrefix;

    @PostConstruct
    public void startConsuming() {
        receiverFactory.createReceiver(userTopic)
            .receive()
            .flatMap(this::processRecord)
            .subscribe();
    }

    private Mono<Void> processRecord(ReceiverRecord<String, String> record) {
        return Mono.fromCallable(() -> objectMapper.readValue(record.value(), UserRegisteredEvent.class))
            .flatMap(event -> {
                log.info("Event received", kv("eventId", event.aggregateId()));
                return userService.upsertUserReplica(event).thenReturn(event);
            })
            .flatMap(event -> {
                String verifyUrl = verifyUrlPrefix + event.verificationToken();
                Map<String, Object> variables = Map.of(
                    "recipient", event.username(),
                    "verificationUrl", verifyUrl
                );

                return templateService.renderTemplate("EMAIL_VERIFICATION", variables)
                    .flatMap(rendered -> emailService.sendHtmlEmail(
                        event.email(),
                        rendered.subject(),
                        rendered.body()
                    ));
            })
            .doOnSuccess(v -> record.receiverOffset().acknowledge())
            .doOnError(e -> log.error("Ошибка при обработке события из Kafka", e))
            .onErrorResume(e -> Mono.empty());
    }
}