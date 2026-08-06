package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.model.OutboxStatus;
import com.portfolio.notification.domain.repository.OutboxRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
    public List<OutboxEvent> findPending(int limit) {

        Pageable pageable =
                PageRequest.of(
                        0,
                        limit
                );

        return springDataRepository.findByStatusOrderByCreatedAtAsc(
                OutboxStatus.PENDING,
                pageable
        );
    }

    @Override
    public Optional<OutboxEvent> findById(UUID id) {
        return springDataRepository.findById(id);
    }
}