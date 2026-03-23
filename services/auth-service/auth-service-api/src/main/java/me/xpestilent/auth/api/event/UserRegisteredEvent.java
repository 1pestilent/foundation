package me.xpestilent.auth.api.event;

import lombok.Builder;
import lombok.Getter;
import me.xpestilent.auth.api.constants.AuthConstants;
import me.xpestilent.foundation.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
public class UserRegisteredEvent implements DomainEvent {

    private UUID eventId;

    private String aggregateId;

    @Builder.Default
    private String aggregateType = AuthConstants.AGGREGATE_USER;

    @Builder.Default
    private String eventType = AuthConstants.EVENT_USER_REGISTERED;

    @Builder.Default
    private Instant createdAt = Instant.now();

    private String username;

    private String email;

    @Override
    public UUID getEventId() {
        return eventId;
    }

    @Override
    public String getAggregateId() {
        return aggregateId;
    }

    @Override
    public String getAggregateType() {
        return aggregateType;
    }

    @Override
    public String getEventType() {
        return eventType;
    }

    @Override
    public Instant getCreatedAt() {
        return createdAt;
    }
}
