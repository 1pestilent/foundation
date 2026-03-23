package me.xpestilent.foundation.event;

import java.time.Instant;
import java.util.UUID;

public interface DomainEvent {
    UUID getEventId();

    String getAggregateId();

    String getAggregateType();

    String getEventType();

    Instant getCreatedAt();
}

