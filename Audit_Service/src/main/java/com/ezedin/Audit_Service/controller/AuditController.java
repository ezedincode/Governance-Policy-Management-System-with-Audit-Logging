package com.ezedin.Audit_Service.controller;

import com.ezedin.Audit_Service.entity.AuditLog;
import com.ezedin.Audit_Service.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class AuditController {

    private final AuditRepository auditRepository;

    @GetMapping("/audit")
    public ResponseEntity<List<AuditLog>> getAllAuditLogs() {
        return ResponseEntity.ok(auditRepository.findAll());
    }
}
