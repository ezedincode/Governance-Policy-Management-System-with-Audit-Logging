package com.ezedin.Audit_Service.service;

import com.ezedin.Audit_Service.dto.GovernanceEvent;
import com.ezedin.Audit_Service.entity.AuditLog;
import com.ezedin.Audit_Service.entity.EventType;
import com.ezedin.Audit_Service.repository.auditRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditConsumerServiceTest {

    @Mock
    private auditRepository auditLogRepository;

    @InjectMocks
    private AuditConsumerService auditConsumerService;

    @Test
    void shouldConsumeEventAndSaveAuditLog() {

        GovernanceEvent event = new GovernanceEvent();
        event.setEventType(EventType.policy_approved);
        event.setPolicyId(15L);
        event.setActor("manager");
        event.setTimestamp("2026-03-14T10:30:00");

        auditConsumerService.consume(event);
        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository, times(1)).save(captor.capture());

        AuditLog saved = captor.getValue();

        assertNotNull(saved);
        assertEquals(EventType.policy_approved, saved.getEventType());
        assertEquals(15L, saved.getPolicyId());
        assertEquals("manager", saved.getActor());
        assertEquals("2026-03-14T10:30:00", saved.getTimestamp());
    }
    @Test
    void shouldThrowOrHandleNullEventGracefully() {
        assertThrows(NullPointerException.class, () -> auditConsumerService.consume(null));
    }

    @Test
    void shouldCallSaveExactlyOnce() {

        GovernanceEvent event = new GovernanceEvent();
        event.setEventType(EventType.policy_approved);
        event.setPolicyId(1L);

        auditConsumerService.consume(event);

        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}