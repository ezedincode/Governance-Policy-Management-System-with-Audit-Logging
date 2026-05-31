package com.ezedin.Governance_Service.dto;

import com.ezedin.Governance_Service.entity.PolicyStatus;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class PolicyResponse {

    private String title;
    private String description;
    private PolicyStatus status;
    private String createdBy;
    private LocalDateTime createdAt;
}
