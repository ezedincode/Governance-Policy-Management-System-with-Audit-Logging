package com.ezedin.Governance_Service.service;

import com.ezedin.Governance_Service.dto.CreatePolicyRequest;
import com.ezedin.Governance_Service.dto.PolicyResponse;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.entity.PolicyStatus;
import com.ezedin.Governance_Service.event.EventType;
import com.ezedin.Governance_Service.event.OutboxEvent;
import com.ezedin.Governance_Service.event.OutboxEventCreated;
import com.ezedin.Governance_Service.exception.InvalidPolicyStateException;
import com.ezedin.Governance_Service.exception.OutboxSerializationException;
import com.ezedin.Governance_Service.exception.PolicyNotFoundException;
import com.ezedin.Governance_Service.repository.PolicyRepository;
import com.ezedin.Governance_Service.repository.outBoxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PolicyService {

    private final PolicyRepository policyRepository;
    private final outBoxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Transactional
    public Policy createPolicy(CreatePolicyRequest request) {
        Policy policy = Policy.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(PolicyStatus.DRAFT)
                .createdAt(LocalDateTime.now())
                .createdBy(request.getCreatedBy())
                .build();
        Policy savedPolicy = policyRepository.save(policy);
        saveOutboxEvent(savedPolicy, EventType.policy_created);
        return savedPolicy;
    }

    public List<PolicyResponse> getAllPolicies() {
        List<Policy> policyResponseList = policyRepository.findAll();
        return policyResponseList.stream()
                .map(this::toPolicyResponse)
                .collect(Collectors.toList());
    }

    public PolicyResponse getPolicyById(Long id) {
        Policy policy = policyRepository.findById(id)
                .orElseThrow(() -> new PolicyNotFoundException("Policy not found with id: " + id));
        return toPolicyResponse(policy);
    }

    @Transactional
    public PolicyResponse submitForApproval(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException(
                        "Policy not found with id: " + policyId));

        if (policy.getStatus() != PolicyStatus.DRAFT) {
            throw new InvalidPolicyStateException(
                    "Only DRAFT policies can be submitted for approval");
        }

        policy.setStatus(PolicyStatus.PENDING_APPROVAL);
        Policy savedPolicy = policyRepository.save(policy);
        saveOutboxEvent(savedPolicy, EventType.policy_submitted);
        return toPolicyResponse(savedPolicy);
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
                .orElseThrow(() -> new PolicyNotFoundException(
                        "Policy not found with id: " + policyId));
        if (policy.getStatus() != PolicyStatus.PENDING_APPROVAL) {
            throw new InvalidPolicyStateException(
                    "Only policies in PENDING_APPROVAL status can be approved");
        }
        policy.setStatus(PolicyStatus.APPROVED);
        Policy savedPolicy = policyRepository.save(policy);
        saveOutboxEvent(savedPolicy, EventType.policy_approved);
        return toPolicyResponse(savedPolicy);
    }

    @Transactional
    public PolicyResponse rejectPolicy(Long policyId) {
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new PolicyNotFoundException(
                        "Policy not found with id: " + policyId));
        if (policy.getStatus() != PolicyStatus.PENDING_APPROVAL) {
            throw new InvalidPolicyStateException(
                    "Only policies in PENDING_APPROVAL status can be rejected");
        }
        policy.setStatus(PolicyStatus.REJECTED);
        Policy savedPolicy = policyRepository.save(policy);
        saveOutboxEvent(savedPolicy, EventType.policy_rejected);
        return toPolicyResponse(savedPolicy);
    }

    private void saveOutboxEvent(Policy policy, EventType eventType) {
        try {
            OutboxEvent event = new OutboxEvent();
            event.setEventType(eventType.name());
            event.setAggregateId(policy.getId().toString());
            event.setPayload(objectMapper.writeValueAsString(policy));
            event.setCreatedAt(LocalDateTime.now());
            OutboxEvent savedEvent = outboxEventRepository.save(event);
            applicationEventPublisher.publishEvent(new OutboxEventCreated(savedEvent.getId()));
        } catch (Exception e) {
            throw new OutboxSerializationException("Failed to serialize policy for outbox event", e);
        }
    }
}
