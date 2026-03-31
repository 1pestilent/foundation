package me.xpestilent.notification.repository;

import me.xpestilent.notification.entity.NotificationTemplateEntity;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface NotificationTemplateRepository extends R2dbcRepository<NotificationTemplateEntity, Long> {

    Mono<NotificationTemplateEntity> findByCode(String code);
}
