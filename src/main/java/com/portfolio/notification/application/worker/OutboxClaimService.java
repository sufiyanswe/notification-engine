package com.portfolio.notification.application.worker;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.repository.OutboxRepository;
import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class OutboxClaimService {

    private final OutboxRepository outboxRepository;
    private final ApplicationProperties applicationProperties;

    public OutboxClaimService(
            OutboxRepository outboxRepository,
            ApplicationProperties applicationProperties
    ) {
        this.outboxRepository = outboxRepository;
        this.applicationProperties = applicationProperties;
    }

    @Transactional
    public List<UUID> claimPendingEvents(
            int limit,
            Instant now
    ) {

        List<OutboxEvent> events =
                outboxRepository.findPendingForClaim(
                        limit,
                        now
                );

        Instant leaseUntil =
                now.plusMillis(
                        applicationProperties
                                .worker()
                                .leaseDurationMs()
                );

        for (OutboxEvent event : events) {

            event.markAsProcessing(
                    leaseUntil
            );
        }

        return events.stream()
                .map(OutboxEvent::getId)
                .toList();
    }
}