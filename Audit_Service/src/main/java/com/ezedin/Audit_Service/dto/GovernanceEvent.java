package com.ezedin.Audit_Service.dto;

import com.ezedin.Audit_Service.entity.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class GovernanceEvent {
        private EventType eventType;
        private Long policyId;
        private String actor;
        private String timestamp;

}
