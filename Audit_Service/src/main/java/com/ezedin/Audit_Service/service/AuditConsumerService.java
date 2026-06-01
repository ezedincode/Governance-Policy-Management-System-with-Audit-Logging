package com.ezedin.Audit_Service.service;

import com.ezedin.Audit_Service.dto.GovernanceEvent;
import com.ezedin.Audit_Service.entity.AuditLog;
import com.ezedin.Audit_Service.repository.auditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class AuditConsumerService {
    private final auditRepository auditLogRepository;

    @KafkaListener(topics = "governance-events")
    public void consume(GovernanceEvent event) {

        AuditLog auditLog = AuditLog.builder()
                .eventType(event.getEventType())
                .policyId(event.getPolicyId())
                .actor(event.getActor())
                .timestamp(event.getTimestamp())
                .build();
        auditLogRepository.save(auditLog);
    }
}
