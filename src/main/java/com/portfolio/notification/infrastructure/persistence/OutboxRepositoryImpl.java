package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.repository.OutboxRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class OutboxRepositoryImpl
        implements OutboxRepository {

    private final SpringDataOutboxRepository springDataRepository;

    public OutboxRepositoryImpl(
            SpringDataOutboxRepository springDataRepository
    ) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public OutboxEvent save(OutboxEvent outboxEvent) {
        return springDataRepository.save(outboxEvent);
    }

    @Override
    public List<OutboxEvent> findPendingForClaim(
            int limit,
            Instant now
    ) {

        return springDataRepository
                .findPendingForClaim(
                        limit,
                        now
                );
    }
    @Override
    public List<OutboxEvent> findExpiredProcessingForRecovery(
            int limit,
            Instant now
    ) {

        return springDataRepository
                .findExpiredProcessingForRecovery(
                        limit,
                        now
                );
    }

    @Override
    public Optional<OutboxEvent> findById(UUID id) {
        return springDataRepository.findById(id);
    }
}