package com.ezedin.Governance_Service.event;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GovernanceEvent {

    private EventType eventType;
    private Long policyId;
    private String actor;
    private String timestamp;

}