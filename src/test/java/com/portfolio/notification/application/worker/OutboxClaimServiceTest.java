package com.portfolio.notification.application.worker;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.repository.OutboxRepository;
import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxClaimServiceTest {

    private OutboxRepository outboxRepository;

    private OutboxClaimService outboxClaimService;

    private ApplicationProperties applicationProperties;

    @BeforeEach
    void setUp() {

        outboxRepository =
                mock(OutboxRepository.class);

        ApplicationProperties.Worker worker =
                new ApplicationProperties.Worker(
                        10,
                        1000,
                        30_000,
                        5_000
                );

        ApplicationProperties.Retry retry =
                new ApplicationProperties.Retry(
                        5,
                        1000,
                        2.0
                );

        applicationProperties =
                new ApplicationProperties(
                        "notification-engine",
                        "0.6.0-SNAPSHOT",
                        worker,
                        retry
                );

        outboxClaimService =
                new OutboxClaimService(
                        outboxRepository,
                        applicationProperties
                );
    }

    @Test
    void shouldClaimPendingEventsAndReturnTheirIds() {

        UUID firstEventId =
                UUID.randomUUID();

        UUID secondEventId =
                UUID.randomUUID();

        Instant now =
                Instant.parse(
                        "2026-08-11T01:00:00Z"
                );

        Instant expectedLeaseUntil =
                Instant.parse(
                        "2026-08-11T01:00:30Z"
                );

        OutboxEvent firstEvent =
                mock(OutboxEvent.class);

        OutboxEvent secondEvent =
                mock(OutboxEvent.class);

        when(firstEvent.getId())
                .thenReturn(firstEventId);

        when(secondEvent.getId())
                .thenReturn(secondEventId);

        when(
                outboxRepository.findPendingForClaim(
                        10,
                        now
                )
        ).thenReturn(
                List.of(
                        firstEvent,
                        secondEvent
                )
        );

        List<UUID> claimedEventIds =
                outboxClaimService.claimPendingEvents(
                        10,
                        now
                );

        assertEquals(
                List.of(
                        firstEventId,
                        secondEventId
                ),
                claimedEventIds
        );

        verify(firstEvent)
                .markAsProcessing(
                        expectedLeaseUntil
                );

        verify(secondEvent)
                .markAsProcessing(
                        expectedLeaseUntil
                );

        verify(
                outboxRepository
        ).findPendingForClaim(
                10,
                now
        );
    }

    @Test
    void shouldReturnEmptyListWhenNoPendingEventsExist() {

        Instant now =
                Instant.parse(
                        "2026-08-11T01:00:00Z"
                );

        when(
                outboxRepository.findPendingForClaim(
                        10,
                        now
                )
        ).thenReturn(
                List.of()
        );

        List<UUID> claimedEventIds =
                outboxClaimService.claimPendingEvents(
                        10,
                        now
                );

        assertTrue(
                claimedEventIds.isEmpty()
        );

        verify(
                outboxRepository
        ).findPendingForClaim(
                10,
                now
        );
    }

    @Test
    void shouldUseConfiguredLeaseDuration() {

        UUID eventId =
                UUID.randomUUID();

        Instant now =
                Instant.parse(
                        "2026-08-11T01:30:00Z"
                );

        Instant expectedLeaseUntil =
                Instant.parse(
                        "2026-08-11T01:30:30Z"
                );

        OutboxEvent event =
                mock(OutboxEvent.class);

        when(event.getId())
                .thenReturn(eventId);

        when(
                outboxRepository.findPendingForClaim(
                        5,
                        now
                )
        ).thenReturn(
                List.of(event)
        );

        outboxClaimService.claimPendingEvents(
                5,
                now
        );

        verify(event)
                .markAsProcessing(
                        expectedLeaseUntil
                );
    }

    @Test
    void shouldPassRequestedLimitAndCurrentTimeToRepository() {

        int limit = 5;

        Instant now =
                Instant.parse(
                        "2026-08-11T01:30:00Z"
                );

        when(
                outboxRepository.findPendingForClaim(
                        limit,
                        now
                )
        ).thenReturn(
                List.of()
        );

        outboxClaimService.claimPendingEvents(
                limit,
                now
        );

        verify(
                outboxRepository
        ).findPendingForClaim(
                limit,
                now
        );
    }
}