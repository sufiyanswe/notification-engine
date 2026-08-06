package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.model.OutboxStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository
        extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findByStatusOrderByCreatedAtAsc(
            OutboxStatus status,
            Pageable pageable
    );

}