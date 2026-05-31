package com.ezedin.Governance_Service.service;

import com.ezedin.Governance_Service.dto.CreatePolicyRequest;
import com.ezedin.Governance_Service.dto.PolicyResponse;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.entity.PolicyStatus;
import com.ezedin.Governance_Service.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
    public List<PolicyResponse> getAllPolicies() {
        List<Policy> policyResponseList = policyRepository.findAll();
        return policyResponseList.stream()
                .map(this::toPolicyResponse)
                .collect(Collectors.toList());
    }

    public PolicyResponse getPolicyById(Integer id) {
        Optional<Policy> response = policyRepository.findById(id);
        return response.map(this::toPolicyResponse).orElse(null);
    }

    public PolicyResponse toPolicyResponse(Policy policy) {
        return PolicyResponse.builder()
               .title(policy.getTitle())
               .description(policy.getDescription())
               .status(policy.getStatus())
               .createdAt(policy.getCreatedAt())
               .createdBy(policy.getCreatedBy())
               .build();
    }
}
