package com.ezedin.Governance_Service.service;

import com.ezedin.Governance_Service.dto.CreatePolicyRequest;
import com.ezedin.Governance_Service.dto.PolicyResponse;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.entity.PolicyStatus;
import com.ezedin.Governance_Service.event.EventType;
import com.ezedin.Governance_Service.event.OutboxEvent;
import com.ezedin.Governance_Service.repository.PolicyRepository;
import com.ezedin.Governance_Service.repository.outBoxEventRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private outBoxEventRepository outboxEventRepository;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PolicyService policyService;

    private void stubObjectMapper() throws Exception {
        when(objectMapper.writeValueAsString(any(Policy.class))).thenReturn("{\"id\":1}");
    }

    @Test
    void shouldCreatePolicySuccessfully() throws Exception {
        stubObjectMapper();

        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setTitle("Test policy");
        request.setDescription("Test description");
        request.setCreatedBy("admin");

        Policy savedPolicy = new Policy();
        savedPolicy.setId(1L);
        savedPolicy.setStatus(PolicyStatus.DRAFT);
        savedPolicy.setCreatedBy("admin");

        when(policyRepository.save(any(Policy.class))).thenReturn(savedPolicy);

        Policy response = policyService.createPolicy(request);

        assertNotNull(response);
        verify(policyRepository).save(any(Policy.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertEquals(EventType.policy_created.name(), captor.getValue().getEventType());
        assertEquals("1", captor.getValue().getAggregateId());
    }

    @Test
    void shouldReturnPolicyById() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setStatus(PolicyStatus.DRAFT);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        PolicyResponse response = policyService.getPolicyById(1L);

        assertNotNull(response);
        verify(policyRepository).findById(1L);
    }

    @Test
    void shouldReturnAllPolicies() {
        Policy policy = new Policy();
        policy.setId(1L);

        when(policyRepository.findAll()).thenReturn(List.of(policy));

        List<PolicyResponse> result = policyService.getAllPolicies();

        assertEquals(1, result.size());
        verify(policyRepository).findAll();
    }

    @Test
    void shouldReturnEmptyListWhenNoPoliciesExist() {
        when(policyRepository.findAll()).thenReturn(List.of());

        List<PolicyResponse> result = policyService.getAllPolicies();

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void shouldSubmitDraftPolicyForApproval() throws Exception {
        stubObjectMapper();

        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.DRAFT);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PolicyResponse response = policyService.submitForApproval(1L);

        assertNotNull(response);
        assertEquals(PolicyStatus.PENDING_APPROVAL, policy.getStatus());
        verify(policyRepository).findById(1L);
        verify(policyRepository).save(any(Policy.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertEquals(EventType.policy_submitted.name(), captor.getValue().getEventType());
    }

    @Test
    void shouldThrowWhenPolicyNotFound() {
        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.submitForApproval(1L)
        );

        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPolicyIsNotDraft() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.APPROVED);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        assertThrows(
                IllegalStateException.class,
                () -> policyService.submitForApproval(1L)
        );

        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldApprovePendingPolicy() throws Exception {
        stubObjectMapper();

        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.PENDING_APPROVAL);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PolicyResponse response = policyService.approvePolicy(1L);

        assertNotNull(response);
        assertEquals(PolicyStatus.APPROVED, policy.getStatus());
        verify(policyRepository).findById(1L);
        verify(policyRepository).save(any(Policy.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertEquals(EventType.policy_approved.name(), captor.getValue().getEventType());
    }

    @Test
    void shouldThrowWhenPolicyNotFoundOnApprove() {
        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.approvePolicy(1L)
        );

        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPolicyNotPendingApprovalForApproval() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setStatus(PolicyStatus.DRAFT);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        assertThrows(
                IllegalStateException.class,
                () -> policyService.approvePolicy(1L)
        );

        verify(policyRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldRejectPendingPolicy() throws Exception {
        stubObjectMapper();

        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.PENDING_APPROVAL);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        PolicyResponse response = policyService.rejectPolicy(1L);

        assertNotNull(response);
        assertEquals(PolicyStatus.REJECTED, policy.getStatus());
        verify(policyRepository).findById(1L);
        verify(policyRepository).save(any(Policy.class));
        verify(outboxEventRepository).save(any(OutboxEvent.class));

        ArgumentCaptor<OutboxEvent> captor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        assertEquals(EventType.policy_rejected.name(), captor.getValue().getEventType());
    }

    @Test
    void shouldThrowWhenPolicyNotFoundOnReject() {
        when(policyRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.rejectPolicy(1L)
        );

        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenPolicyNotPendingApprovalForReject() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setStatus(PolicyStatus.DRAFT);

        when(policyRepository.findById(1L)).thenReturn(Optional.of(policy));

        assertThrows(
                IllegalStateException.class,
                () -> policyService.rejectPolicy(1L)
        );

        verify(policyRepository).findById(1L);
        verify(policyRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }
}
