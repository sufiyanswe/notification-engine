package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.repository.OutboxRepository;
import org.springframework.stereotype.Repository;

@Repository
public class OutboxRepositoryImpl
        implements OutboxRepository {

    private final SpringDataOutboxRepository repository;

    public OutboxRepositoryImpl(
            SpringDataOutboxRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return repository.save(outboxEvent);
    }
}