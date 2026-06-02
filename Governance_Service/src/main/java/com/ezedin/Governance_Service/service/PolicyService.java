package com.ezedin.Governance_Service.service;

import com.ezedin.Governance_Service.dto.CreatePolicyRequest;
import com.ezedin.Governance_Service.dto.PolicyResponse;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.entity.PolicyStatus;
import com.ezedin.Governance_Service.event.EventType;
import com.ezedin.Governance_Service.event.GovernanceEventProducer;
import com.ezedin.Governance_Service.repository.PolicyRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final GovernanceEventProducer eventProducer;

    public Policy createPolicy(CreatePolicyRequest request) {
        Policy policy = Policy.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(PolicyStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .build();
                Policy savedPolicy = policyRepository.save(policy);
                eventProducer.publish(EventType.policy_created, savedPolicy.getId(), policy.getCreatedBy());
                return savedPolicy;
    }
    public List<PolicyResponse> getAllPolicies() {
        List<Policy> policyResponseList = policyRepository.findAll();
        return policyResponseList.stream()
                .map(this::toPolicyResponse)
                .collect(Collectors.toList());
    }

    public PolicyResponse getPolicyById(Long id) {
        Optional<Policy> response = policyRepository.findById(id);
        return response.map(this::toPolicyResponse).orElse(null);
    }

    @Transactional
    public PolicyResponse submitForApproval(Long policyId) {

        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Policy not found with id: " + policyId));

        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only DRAFT policies can be submitted for approval");
        }

        policy.setStatus(PolicyStatus.PENDING_APPROVAL);
        Policy savedPolicy = policyRepository.save(policy);
        PolicyResponse response = toPolicyResponse(savedPolicy);
        eventProducer.publish(EventType.policy_submitted, savedPolicy.getId(), savedPolicy.getCreatedBy());
        return response;
    }
    public PolicyResponse toPolicyResponse(Policy policy) {
        return PolicyResponse.builder()
                .Id(policy.getId())
               .title(policy.getTitle())
               .description(policy.getDescription())
               .status(policy.getStatus())
               .createdAt(policy.getCreatedAt())
               .createdBy(policy.getCreatedBy())
               .build();
    }

    @Transactional
    public PolicyResponse approvePolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "policy not found with id: " + policyId));
        if (policy.getStatus() != PolicyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "only policies in PENDING_APPROVAL status can be approved");
        }
        policy.setStatus(PolicyStatus.APPROVED);
        Policy savedPolicy = policyRepository.save(policy);
        PolicyResponse response = toPolicyResponse(savedPolicy);
        eventProducer.publish(EventType.policy_approved, savedPolicy.getId(), savedPolicy.getCreatedBy());
        return response;
    }
    @Transactional
    public PolicyResponse rejectPolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "policy not found with id: " + policyId));
        if (policy.getStatus() != PolicyStatus.PENDING_APPROVAL) {
            throw new IllegalStateException(
                    "only policies in PENDING_APPROVAL status can be rejected");
        }
        policy.setStatus(PolicyStatus.REJECTED);
        Policy savedPolicy = policyRepository.save(policy);
        eventProducer.publish(EventType.policy_rejected, savedPolicy.getId(), savedPolicy.getCreatedBy());
        return toPolicyResponse(savedPolicy);
    }
}
