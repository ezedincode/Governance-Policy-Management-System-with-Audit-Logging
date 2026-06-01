package com.ezedin.Governance_Service.event;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GovernanceEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceEventProducer.class);

    @Value("${kafka.topic.governance-events}")
    private String topic;

    private final KafkaTemplate<String, GovernanceEvent> kafkaTemplate;

    public void publish(EventType eventType, Long policyId, String actor) {

        GovernanceEvent event = new GovernanceEvent();
        event.setEventType(eventType);
        event.setPolicyId(policyId);
        event.setActor(actor);
        event.setTimestamp(LocalDateTime.now().toString());
        kafkaTemplate.send(topic, policyId.toString(), event);

    }
}