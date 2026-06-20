package com.ezedin.Audit_Service.service;

import com.ezedin.Audit_Service.dto.GovernanceEvent;
import com.ezedin.Audit_Service.entity.AuditLog;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuditConsumerService {

    private final AuditLogWriter auditLogWriter;

    @KafkaListener(topics = "governance-events")
    public void consume(GovernanceEvent event) {
        AuditLog auditLog = AuditLog.builder()
                .eventType(event.getEventType())
                .policyId(event.getPolicyId())
                .actor(event.getActor())
                .timestamp(event.getTimestamp())
                .build();
        auditLogWriter.save(auditLog);
    }
}
