package com.portfolio.notification.domain.repository;

import com.portfolio.notification.domain.model.OutboxEvent;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepository {

    Optional<OutboxEvent> findById(UUID id);

    List<OutboxEvent> findPendingForClaim(int limit, Instant now);

    List<OutboxEvent> findExpiredProcessingForRecovery(
            int limit,
            Instant now
    );

    OutboxEvent save(OutboxEvent outboxEvent);



}