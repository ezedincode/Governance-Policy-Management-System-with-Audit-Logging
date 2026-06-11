package com.ezedin.Governance_Service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "outbox.publisher")
public class OutboxPublisherProperties {

    private int maxRetries = 5;
    private long pollIntervalMs = 1000;
    private long kafkaSendTimeoutMs = 5000;
}
