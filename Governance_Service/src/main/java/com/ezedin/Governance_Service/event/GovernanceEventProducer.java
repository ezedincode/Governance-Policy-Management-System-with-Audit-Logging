package com.ezedin.Governance_Service.event;

import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.repository.outBoxEventRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GovernanceEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceEventProducer.class);

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, GovernanceEvent> kafkaTemplate;
    private final outBoxEventRepository outboxEventRepository;

    @Value("${kafka.topic.governance-events}")
    private String topic;

    @Scheduled(fixedDelay = 3000)
    @Transactional
    public void publish() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalse();

        for (OutboxEvent event : events) {
            try {
                Policy policy = objectMapper.readValue(event.getPayload(), Policy.class);

                GovernanceEvent governanceEvent = new GovernanceEvent();
                governanceEvent.setEventType(EventType.valueOf(event.getEventType()));
                governanceEvent.setPolicyId(Long.parseLong(event.getAggregateId()));
                governanceEvent.setActor(policy.getCreatedBy());
                governanceEvent.setTimestamp(event.getCreatedAt().toString());

                kafkaTemplate.send(topic, event.getAggregateId(), governanceEvent);

                event.setProcessed(true);
                outboxEventRepository.save(event);
            } catch (Exception e) {
                LOGGER.error("Failed to publish outbox event id={}", event.getId(), e);
            }
        }
    }
}
