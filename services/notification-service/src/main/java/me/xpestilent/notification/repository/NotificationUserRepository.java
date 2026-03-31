package me.xpestilent.notification.repository;

import me.xpestilent.notification.entity.NotificationUserEntity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

public interface NotificationUserRepository extends R2dbcRepository<NotificationUserEntity, UUID> {
    @Query("""
            INSERT INTO notification_schema.notification_users (id, username, email, created_at)
            VALUES (:id, :username, :email, :now)
            ON CONFLICT (id) DO UPDATE
            SET username = EXCLUDED.username,
                email = EXCLUDED.email,
                updated_at = :now
            RETURNING *
        """)
    Mono<NotificationUserEntity> upsertUser(UUID id, String username, String email, Instant now);
}