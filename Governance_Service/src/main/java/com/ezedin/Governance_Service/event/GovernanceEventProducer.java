package com.ezedin.Governance_Service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GovernanceEventProducer {

    @Value("${kafka.topic.governance-events}")
    private String topic;

    private final KafkaTemplate<String, GovernanceEvent> kafkaTemplate;

    public void publish(EventType eventType, Long policyId, String actor) {

        GovernanceEvent event = new GovernanceEvent();
        event.setEventType(eventType);
        event.setPolicyId(policyId);
        event.setActor(actor);
        event.setTimestamp(LocalDateTime.now());

        kafkaTemplate.send(topic, event);
    }
}