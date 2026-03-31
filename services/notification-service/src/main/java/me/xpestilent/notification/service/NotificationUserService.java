package me.xpestilent.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.notification.entity.NotificationUserEntity;
import me.xpestilent.notification.event.UserRegisteredEvent;
import me.xpestilent.notification.repository.NotificationUserRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationUserService {

    private final NotificationUserRepository userRepository;

    /**
     * Создает нового или обновляет существующего пользователя
     */
    public Mono<NotificationUserEntity> upsertUserReplica(UserRegisteredEvent event) {
        return userRepository.upsertUser(
            UUID.fromString(event.aggregateId()),
            event.username(),
            event.email(),
            Instant.now()
        ).doOnSuccess(u -> log.info("User has been replicated", kv("userId", event.aggregateId())));
    }
}