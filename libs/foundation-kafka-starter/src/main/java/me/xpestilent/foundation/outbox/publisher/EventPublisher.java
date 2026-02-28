package me.xpestilent.foundation.outbox.publisher;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.xpestilent.foundation.event.DomainEvent;
import me.xpestilent.foundation.outbox.entity.OutboxEntity;
import me.xpestilent.foundation.outbox.repository.OutboxRepository;
import me.xpestilent.foundation.web.exception.SystemException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisher {

    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Transactional(propagation = Propagation.MANDATORY)
    public void publish(DomainEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            String traceId = MDC.get("traceId");

            OutboxEntity outboxEntity = OutboxEntity.builder()
                .id(event.getEventId())
                .aggregateType(event.getAggregateType())
                .aggregateId(event.getAggregateId())
                .type(event.getEventType())
                .payload(payload)
                .traceId(traceId)
                .build();

            outboxRepository.save(outboxEntity);

            log.debug("Published outbox event",
                kv("eventId", event.getEventId()),
                kv("eventType", event.getEventType()),
                kv("aggregateType", event.getAggregateType()),
                kv("aggregateId", event.getAggregateId()));

        } catch (JsonProcessingException e) {

            Map<String, Object> errorDetails = Map.of(
                "eventId", event.getEventId(),
                "eventType", event.getEventType(),
                "aggregateId", event.getAggregateId()
            );

            throw new SystemException(
                "Failed to serialize event for outbox",
                "OUTBOX_SERIALIZATION_ERROR",
                e,
                errorDetails
            );
        }
    }
}