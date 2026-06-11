package com.ezedin.Governance_Service.event;

import com.ezedin.Governance_Service.config.OutboxPublisherProperties;
import com.ezedin.Governance_Service.repository.outBoxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OutboxEventPublisher {

    private static final Logger LOGGER = LoggerFactory.getLogger(OutboxEventPublisher.class);

    private final outBoxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, GovernanceEvent> kafkaTemplate;
    private final OutboxPublisherProperties properties;

    @Value("${kafka.topic.governance-events-dlq}")
    private String deadLetterTopic;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAsProcessed(Long outboxEventId) {
        OutboxEvent event = outboxEventRepository.findByIdForUpdate(outboxEventId)
                .orElseThrow(() -> new IllegalStateException("Outbox event not found: " + outboxEventId));

        if (event.isProcessed() || event.isFailed()) {
            return;
        }

        event.setProcessed(true);
        event.setProcessedAt(LocalDateTime.now());
        event.setLastError(null);
        outboxEventRepository.save(event);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordFailure(Long outboxEventId, GovernanceEvent governanceEvent, String errorMessage) {
        OutboxEvent event = outboxEventRepository.findByIdForUpdate(outboxEventId)
                .orElseThrow(() -> new IllegalStateException("Outbox event not found: " + outboxEventId));

        if (event.isProcessed() || event.isFailed()) {
            return;
        }

        event.setRetryCount(event.getRetryCount() + 1);
        event.setLastError(errorMessage);

        if (event.getRetryCount() >= properties.getMaxRetries()) {
            event.setFailed(true);
            outboxEventRepository.save(event);
            sendToDeadLetterQueue(event, governanceEvent, errorMessage);
            LOGGER.error(
                    "Outbox event id={} moved to DLQ after {} retries. Last error: {}",
                    outboxEventId,
                    properties.getMaxRetries(),
                    errorMessage
            );
            return;
        }

        outboxEventRepository.save(event);
        LOGGER.warn(
                "Outbox event id={} publish failed (attempt {}/{}): {}",
                outboxEventId,
                event.getRetryCount(),
                properties.getMaxRetries(),
                errorMessage
        );
    }

    private void sendToDeadLetterQueue(OutboxEvent event, GovernanceEvent governanceEvent, String errorMessage) {
        try {
            GovernanceEvent deadLetterEvent = governanceEvent != null
                    ? governanceEvent
                    : buildFallbackDeadLetterEvent(event);

            kafkaTemplate.send(deadLetterTopic, event.getAggregateId(), deadLetterEvent)
                    .get(properties.getKafkaSendTimeoutMs(), java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception ex) {
            LOGGER.error("Failed to publish outbox event id={} to DLQ topic", event.getId(), ex);
        }
    }

    private GovernanceEvent buildFallbackDeadLetterEvent(OutboxEvent event) {
        GovernanceEvent deadLetterEvent = new GovernanceEvent();
        deadLetterEvent.setEventType(EventType.valueOf(event.getEventType()));
        deadLetterEvent.setPolicyId(Long.parseLong(event.getAggregateId()));
        deadLetterEvent.setActor("unknown");
        deadLetterEvent.setTimestamp(event.getCreatedAt().toString());
        return deadLetterEvent;
    }
}
