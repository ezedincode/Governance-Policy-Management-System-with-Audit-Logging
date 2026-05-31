package com.ezedin.Governance_Service.controller;

import com.ezedin.Governance_Service.dto.CreatePolicyRequest;
import com.ezedin.Governance_Service.dto.PolicyResponse;
import com.ezedin.Governance_Service.entity.Policy;
import com.ezedin.Governance_Service.service.policyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class policyController {
    private final policyService policyService;

    @PostMapping("/policy")
    public ResponseEntity<Policy> createPolicy(
             @Valid @RequestBody CreatePolicyRequest request) {

        Policy policy = policyService.createPolicy(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(policy);
    }

    @GetMapping("/policies")
    public ResponseEntity<List<PolicyResponse>> getAllPolicies() {
        List<PolicyResponse> response = policyService.getAllPolicies();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/policies/{id}")
    public ResponseEntity<PolicyResponse> getPolicyById(@PathVariable int id) {
        return ResponseEntity.ok(policyService.getPolicyById(id));
    }

}
