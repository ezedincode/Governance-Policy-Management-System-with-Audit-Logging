package com.ezedin.Governance_Service.event;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ResilientKafkaSender {

    private final KafkaTemplate<String, GovernanceEvent> kafkaTemplate;

    @CircuitBreaker(name = "kafka-producer", fallbackMethod = "sendFallback")
    public void send(String topic, String key, GovernanceEvent event, long timeoutMs) throws Exception {
        kafkaTemplate.send(topic, key, event).get(timeoutMs, TimeUnit.MILLISECONDS);
    }

    @SuppressWarnings("unused")
    private void sendFallback(String topic, String key, GovernanceEvent event, long timeoutMs, Throwable t)
            throws Exception {
        String message = t.getMessage() != null ? t.getMessage() : "Kafka publish failed";
        throw new Exception(message, t);
    }
}
