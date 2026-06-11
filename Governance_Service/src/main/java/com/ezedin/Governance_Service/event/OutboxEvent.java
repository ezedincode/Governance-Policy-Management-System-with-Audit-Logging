package com.ezedin.Governance_Service.event;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue
    private Long id;

    private String eventType;

    private String aggregateId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;

    private boolean processed = false;

    private boolean failed = false;

    private int retryCount = 0;

    @Column(columnDefinition = "TEXT")
    private String lastError;

    private LocalDateTime createdAt;

    private LocalDateTime processedAt;
}
