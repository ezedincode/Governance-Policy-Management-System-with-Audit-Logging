package com.ezedin.Governance_Service.event;

import com.ezedin.Governance_Service.config.OutboxPublisherProperties;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.entity.PolicyStatus;
import com.ezedin.Governance_Service.repository.outBoxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GovernanceEventProducerTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private KafkaTemplate<String, GovernanceEvent> kafkaTemplate;

    @Mock
    private outBoxEventRepository outboxEventRepository;

    @Mock
    private OutboxEventPublisher outboxEventPublisher;

    @Mock
    private OutboxPublisherProperties properties;

    @InjectMocks
    private GovernanceEventProducer governanceEventProducer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(governanceEventProducer, "topic", "governance-events");
        when(properties.getKafkaSendTimeoutMs()).thenReturn(5000L);
    }

    @Test
    void shouldPublishEventAndMarkProcessedOnKafkaAck() throws Exception {
        OutboxEvent outboxEvent = buildOutboxEvent();
        Policy policy = buildPolicy();

        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue(outboxEvent.getPayload(), Policy.class)).thenReturn(policy);
        when(kafkaTemplate.send(eq("governance-events"), eq("1"), any(GovernanceEvent.class)))
                .thenReturn(CompletableFuture.completedFuture(mock(SendResult.class)));

        governanceEventProducer.onOutboxEventCreated(new OutboxEventCreated(1L));

        verify(kafkaTemplate).send(eq("governance-events"), eq("1"), any(GovernanceEvent.class));
        verify(outboxEventPublisher).markAsProcessed(1L);
        verify(outboxEventPublisher, never()).recordFailure(any(), any(), any());
    }

    @Test
    void shouldRecordFailureWhenKafkaPublishFails() throws Exception {
        OutboxEvent outboxEvent = buildOutboxEvent();
        Policy policy = buildPolicy();

        when(outboxEventRepository.findById(1L)).thenReturn(Optional.of(outboxEvent));
        when(objectMapper.readValue(outboxEvent.getPayload(), Policy.class)).thenReturn(policy);
        when(kafkaTemplate.send(eq("governance-events"), eq("1"), any(GovernanceEvent.class)))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("Kafka unavailable")));

        governanceEventProducer.onOutboxEventCreated(new OutboxEventCreated(1L));

        verify(outboxEventPublisher, never()).markAsProcessed(any());
        verify(outboxEventPublisher).recordFailure(eq(1L), any(GovernanceEvent.class), eq("Kafka unavailable"));
    }

    private OutboxEvent buildOutboxEvent() {
        OutboxEvent event = new OutboxEvent();
        event.setId(1L);
        event.setEventType(EventType.policy_created.name());
        event.setAggregateId("1");
        event.setPayload("{\"id\":1}");
        event.setCreatedAt(LocalDateTime.now());
        return event;
    }

    private Policy buildPolicy() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.DRAFT);
        return policy;
    }
}
