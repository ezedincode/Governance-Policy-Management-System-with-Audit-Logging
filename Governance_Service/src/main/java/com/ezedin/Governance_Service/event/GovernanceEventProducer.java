package com.ezedin.Governance_Service.event;

import com.ezedin.Governance_Service.config.OutboxPublisherProperties;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.repository.outBoxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GovernanceEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceEventProducer.class);

    private final ObjectMapper objectMapper;
    private final ResilientKafkaSender resilientKafkaSender;
    private final outBoxEventRepository outboxEventRepository;
    private final OutboxEventPublisher outboxEventPublisher;
    private final OutboxPublisherProperties properties;

    @Value("${kafka.topic.governance-events}")
    private String topic;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onOutboxEventCreated(OutboxEventCreated event) {
        outboxEventRepository.findById(event.outboxEventId())
                .ifPresent(this::publishEvent);
    }

    @Scheduled(fixedDelayString = "${outbox.publisher.poll-interval-ms:1000}")
    public void pollUnprocessedEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseAndFailedFalseOrderByCreatedAtAsc();
        events.forEach(this::publishEvent);
    }

    private void publishEvent(OutboxEvent event) {
        if (event.isProcessed() || event.isFailed()) {
            return;
        }

        GovernanceEvent governanceEvent = null;
        try {
            governanceEvent = toGovernanceEvent(event);
            resilientKafkaSender.send(topic, event.getAggregateId(), governanceEvent,
                    properties.getKafkaSendTimeoutMs());
            outboxEventPublisher.markAsProcessed(event.getId());
        } catch (Exception ex) {
            LOGGER.error("Failed to publish outbox event id={}", event.getId(), ex);
            outboxEventPublisher.recordFailure(event.getId(), governanceEvent, resolveErrorMessage(ex));
        }
    }

    private String resolveErrorMessage(Exception ex) {
        Throwable cause = ex.getCause();
        if (cause != null && cause.getMessage() != null) {
            return cause.getMessage();
        }
        return ex.getMessage();
    }

    private GovernanceEvent toGovernanceEvent(OutboxEvent event) throws Exception {
        Policy policy = objectMapper.readValue(event.getPayload(), Policy.class);

        GovernanceEvent governanceEvent = new GovernanceEvent();
        governanceEvent.setEventType(EventType.valueOf(event.getEventType()));
        governanceEvent.setPolicyId(Long.parseLong(event.getAggregateId()));
        governanceEvent.setActor(policy.getCreatedBy());
        governanceEvent.setTimestamp(event.getCreatedAt().toString());
        return governanceEvent;
    }
}
