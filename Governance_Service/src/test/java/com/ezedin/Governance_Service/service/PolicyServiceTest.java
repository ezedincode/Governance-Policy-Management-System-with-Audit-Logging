package com.ezedin.Governance_Service.service;

import com.ezedin.Governance_Service.dto.PolicyResponse;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.entity.PolicyStatus;
import com.ezedin.Governance_Service.event.EventType;
import com.ezedin.Governance_Service.event.GovernanceEventProducer;
import com.ezedin.Governance_Service.repository.PolicyRepository;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.ezedin.Governance_Service.dto.CreatePolicyRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock
    private PolicyRepository policyRepository;

    @Mock
    private GovernanceEventProducer governanceEventProducer;

    @InjectMocks
    private PolicyService policyService;

    //for policy creation
    @Test
    void shouldCreatePolicySuccessfully() {

        CreatePolicyRequest request = new CreatePolicyRequest();
        request.setTitle("Test policy");
        request.setDescription("Test description");
        request.setCreatedBy("admin");

        Policy savedPolicy = new Policy();
        savedPolicy.setId(1L);
        savedPolicy.setStatus(PolicyStatus.DRAFT);
        savedPolicy.setCreatedBy("admin");
        when(policyRepository.save(any(Policy.class)))
                .thenReturn(savedPolicy);
        Policy response = policyService.createPolicy(request);
        assertNotNull(response);
        verify(policyRepository).save(any(Policy.class));
        verify(governanceEventProducer).publish(EventType.policy_created,
                1L,
                "admin"
        );
    }
   //for getting policy by id
   @Test
   void shouldReturnPolicyById() {

       Policy policy = new Policy();
       policy.setId(1L);
       policy.setStatus(PolicyStatus.DRAFT);

       when(policyRepository.findById(1))
               .thenReturn(Optional.of(policy));

       PolicyResponse response = policyService.getPolicyById(1);

       assertNotNull(response);

       verify(policyRepository).findById(1);
   }

    //for getting all policies
    @Test
    void shouldReturnAllPolicies() {

        Policy policy = new Policy();
        policy.setId(1L);

        when(policyRepository.findAll())
                .thenReturn(List.of(policy));
        List<PolicyResponse> result = policyService.getAllPolicies();
        assertEquals(1, result.size());
        verify(policyRepository).findAll();
    }
    @Test
    void shouldReturnEmptyListWhenNoPoliciesExist() {
        when(policyRepository.findAll())
                .thenReturn(List.of());
        List<PolicyResponse> result = policyService.getAllPolicies();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
    //for submiting policy
    @Test
    void shouldSubmitDraftPolicyForApproval() {

        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.DRAFT);
        when(policyRepository.findById(1))
                .thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PolicyResponse response = policyService.submitForApproval(1);
        assertNotNull(response);
        assertEquals(
                PolicyStatus.PENDING_APPROVAL,
                policy.getStatus());
        verify(policyRepository).findById(1);
        verify(policyRepository).save(any(Policy.class));
        verify(governanceEventProducer).publish(
                EventType.policy_submitted,
                1L,
                "admin"
        );
    }
    @Test
    void shouldThrowWhenPolicyNotFound() {
        when(policyRepository.findById(1))
                .thenReturn(Optional.empty());
        assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.submitForApproval(1)
        );
        verify(policyRepository).findById(1);
        verify(policyRepository, never()).save(any());
        verify(governanceEventProducer, never()).publish(any(), anyLong(), any());
    }
    @Test
    void shouldThrowWhenPolicyIsNotDraft() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.APPROVED);
        when(policyRepository.findById(1))
                .thenReturn(Optional.of(policy));
        assertThrows(
                IllegalStateException.class,
                () -> policyService.submitForApproval(1)
        );
        verify(policyRepository).findById(1);
        verify(policyRepository, never()).save(any());
        verify(governanceEventProducer, never()).publish(any(), anyLong(), any());
    }

    //for approving policy

    @Test
    void shouldApprovePendingPolicy() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.PENDING_APPROVAL);
        when(policyRepository.findById(1))
                .thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PolicyResponse response = policyService.approvePolicy(1);
        assertNotNull(response);
        assertEquals(
                PolicyStatus.APPROVED,
                policy.getStatus()
        );
        verify(policyRepository).findById(1);
        verify(policyRepository).save(any(Policy.class));
        verify(governanceEventProducer).publish(
                EventType.policy_approved,
                1L,
                "admin"
        );
    }
    @Test
    void shouldThrowWhenPolicyNotFoundOnApprove() {
        when(policyRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.approvePolicy(1)
        );

        verify(policyRepository).findById(1);

        verify(policyRepository, never()).save(any());

        verify(governanceEventProducer, never()).publish(any(), anyLong(), any());
    }
    @Test
    void shouldThrowWhenPolicyNotPendingApprovalForApproval() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setStatus(PolicyStatus.DRAFT);
        when(policyRepository.findById(1))
                .thenReturn(Optional.of(policy));
        assertThrows(
                IllegalStateException.class,
                () -> policyService.approvePolicy(1)
        );
        verify(policyRepository, never()).save(any());
        verify(governanceEventProducer, never()).publish(any(), anyLong(), any());
    }
    //for policy rejection
    @Test
    void shouldRejectPendingPolicy() {
        Policy policy = new Policy();
        policy.setId(1L);
        policy.setCreatedBy("admin");
        policy.setStatus(PolicyStatus.PENDING_APPROVAL);
        when(policyRepository.findById(1))
                .thenReturn(Optional.of(policy));
        when(policyRepository.save(any(Policy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        PolicyResponse response = policyService.rejectPolicy(1);
        assertNotNull(response);
        assertEquals(
                PolicyStatus.REJECTED,
                policy.getStatus()
        );
        verify(policyRepository).findById(1);
        verify(policyRepository).save(any(Policy.class));
        verify(governanceEventProducer).publish(
                EventType.policy_rejected,
                1L,
                "admin"
        );
    }
    @Test
    void shouldThrowWhenPolicyNotFoundOnReject() {

        when(policyRepository.findById(1))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceNotFoundException.class,
                () -> policyService.rejectPolicy(1)
        );

        verify(policyRepository).findById(1);

        verify(policyRepository, never()).save(any());

        verify(governanceEventProducer, never()).publish(any(), anyLong(), any());
    }
    @Test
    void shouldThrowWhenPolicyNotPendingApprovalForReject() {

        Policy policy = new Policy();
        policy.setId(1L);
        policy.setStatus(PolicyStatus.DRAFT);
        when(policyRepository.findById(1))
                .thenReturn(Optional.of(policy));
        assertThrows(
                IllegalStateException.class,
                () -> policyService.rejectPolicy(1)
        );
        verify(policyRepository).findById(1);
        verify(policyRepository, never()).save(any());
        verify(governanceEventProducer, never()).publish(any(), anyLong(), any());
    }

}