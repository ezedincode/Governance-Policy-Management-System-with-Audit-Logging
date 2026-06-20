package com.ezedin.Audit_Service.service;

import com.ezedin.Audit_Service.entity.AuditLog;
import com.ezedin.Audit_Service.repository.AuditRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditLogWriter {

    private final AuditRepository auditLogRepository;

    @CircuitBreaker(name = "audit-database", fallbackMethod = "saveFallback")
    public void save(AuditLog auditLog) {
        auditLogRepository.save(auditLog);
    }

    @SuppressWarnings("unused")
    private void saveFallback(AuditLog auditLog, Throwable t) {
        throw new AuditPersistenceException("Audit database is temporarily unavailable", t);
    }
}
