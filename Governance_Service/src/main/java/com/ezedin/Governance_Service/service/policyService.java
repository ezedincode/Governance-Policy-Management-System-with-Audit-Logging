package com.ezedin.Governance_Service.service;

import com.ezedin.Governance_Service.dto.CreatePolicyRequest;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.entity.PolicyStatus;
import com.ezedin.Governance_Service.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class policyService {

    private final PolicyRepository policyRepository;

    public Policy createPolicy(CreatePolicyRequest request) {
        Policy policy = Policy.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(PolicyStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .build();
        return policyRepository.save(policy);
    }
}
