package com.portfolio.notification.application.worker;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class OutboxRecoveryService {

    private final OutboxRepository outboxRepository;

    public OutboxRecoveryService(
            OutboxRepository outboxRepository
    ) {
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public int recoverExpiredEvents(
            int limit,
            Instant now
    ) {

        List<OutboxEvent> events =
                outboxRepository.findExpiredProcessingForRecovery(
                        limit,
                        now
                );

        for (OutboxEvent event : events) {

            event.recoverFromExpiredLease(
                    now
            );
        }

        return events.size();
    }
}