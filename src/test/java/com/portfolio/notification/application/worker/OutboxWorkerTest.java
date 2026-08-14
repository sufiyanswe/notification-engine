package com.portfolio.notification.application.worker;

import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

class OutboxWorkerTest {

    private OutboxClaimService outboxClaimService;
    private OutboxProcessor outboxProcessor;
    private ApplicationProperties applicationProperties;
    private Clock clock;

    private OutboxWorker outboxWorker;

    @BeforeEach
    void setUp() {

        outboxClaimService =
                mock(OutboxClaimService.class);

        outboxProcessor =
                mock(OutboxProcessor.class);

        applicationProperties =
                mock(ApplicationProperties.class);

        ApplicationProperties.Worker worker =
                mock(ApplicationProperties.Worker.class);

        when(applicationProperties.worker())
                .thenReturn(worker);

        when(worker.batchSize())
                .thenReturn(10);

        clock =
                Clock.fixed(
                        Instant.parse(
                                "2026-08-11T01:00:00Z"
                        ),
                        ZoneOffset.UTC
                );

        outboxWorker =
                new OutboxWorker(
                        outboxClaimService,
                        outboxProcessor,
                        applicationProperties,
                        clock
                );
    }

    @Test
    void shouldClaimAndProcessAllClaimedEvents() {

        UUID firstEventId =
                UUID.randomUUID();

        UUID secondEventId =
                UUID.randomUUID();

        Instant now =
                Instant.now(clock);

        when(
                outboxClaimService.claimPendingEvents(
                        10,
                        now
                )
        ).thenReturn(
                List.of(
                        firstEventId,
                        secondEventId
                )
        );

        outboxWorker.processPendingEvents();

        verify(
                outboxClaimService
        ).claimPendingEvents(
                10,
                now
        );

        verify(outboxProcessor)
                .process(firstEventId);

        verify(outboxProcessor)
                .process(secondEventId);
    }

    @Test
    void shouldNotProcessAnythingWhenNoEventsAreClaimed() {

        Instant now =
                Instant.now(clock);

        when(
                outboxClaimService.claimPendingEvents(
                        10,
                        now
                )
        ).thenReturn(
                List.of()
        );

        outboxWorker.processPendingEvents();

        verify(
                outboxClaimService
        ).claimPendingEvents(
                10,
                now
        );

        verifyNoInteractions(
                outboxProcessor
        );
    }

    @Test
    void shouldContinueProcessingWhenOneEventFails() {

        UUID failedEventId =
                UUID.randomUUID();

        UUID successfulEventId =
                UUID.randomUUID();

        Instant now =
                Instant.now(clock);

        when(
                outboxClaimService.claimPendingEvents(
                        10,
                        now
                )
        ).thenReturn(
                List.of(
                        failedEventId,
                        successfulEventId
                )
        );

        doThrow(
                new IllegalStateException(
                        "Unexpected processing failure"
                )
        ).when(outboxProcessor)
                .process(failedEventId);

        outboxWorker.processPendingEvents();

        verify(outboxProcessor)
                .process(failedEventId);

        verify(outboxProcessor)
                .process(successfulEventId);
    }

    @Test
    void shouldUseConfiguredBatchSize() {

        Instant now =
                Instant.now(clock);

        when(
                applicationProperties
                        .worker()
                        .batchSize()
        ).thenReturn(25);

        when(
                outboxClaimService.claimPendingEvents(
                        25,
                        now
                )
        ).thenReturn(
                List.of()
        );

        outboxWorker.processPendingEvents();

        verify(
                outboxClaimService
        ).claimPendingEvents(
                25,
                now
        );
    }
}