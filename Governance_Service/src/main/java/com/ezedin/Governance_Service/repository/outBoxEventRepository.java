package com.ezedin.Governance_Service.repository;

import com.ezedin.Governance_Service.event.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface outBoxEventRepository extends JpaRepository<OutboxEvent ,Long> {
    List<OutboxEvent> findByProcessedFalse();
}
