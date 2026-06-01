package com.ezedin.Audit_Service.repository;

import com.ezedin.Audit_Service.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface auditRepository extends JpaRepository<AuditLog , Long> {
}
