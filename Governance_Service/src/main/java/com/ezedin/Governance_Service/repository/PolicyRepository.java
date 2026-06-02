package com.ezedin.Governance_Service.repository;

import com.ezedin.Governance_Service.entity.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {

}
