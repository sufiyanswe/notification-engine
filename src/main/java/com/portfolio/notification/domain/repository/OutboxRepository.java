package com.portfolio.notification.domain.repository;

import com.portfolio.notification.domain.model.OutboxEvent;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutboxRepository {

    Optional<OutboxEvent> findById(UUID id);

    List<OutboxEvent> findPending(int limit);

    OutboxEvent save(OutboxEvent outboxEvent);

}