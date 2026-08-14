package com.portfolio.notification.application.worker;

import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.mockito.Mockito.*;

class OutboxRecoveryWorkerTest {

    private OutboxRecoveryService outboxRecoveryService;
    private ApplicationProperties applicationProperties;
    private Clock clock;

    private OutboxRecoveryWorker outboxRecoveryWorker;

    @BeforeEach
    void setUp() {

        outboxRecoveryService =
                mock(OutboxRecoveryService.class);

        applicationProperties =
                mock(ApplicationProperties.class);

        ApplicationProperties.Worker worker =
                new ApplicationProperties.Worker(
                        10,
                        1000,
                        30_000,
                        5_000
                );

        when(applicationProperties.worker())
                .thenReturn(worker);

        clock =
                Clock.fixed(
                        Instant.parse(
                                "2026-08-12T20:00:00Z"
                        ),
                        ZoneOffset.UTC
                );

        outboxRecoveryWorker =
                new OutboxRecoveryWorker(
                        outboxRecoveryService,
                        applicationProperties,
                        clock
                );
    }

    @Test
    void shouldRecoverExpiredEventsUsingConfiguredBatchSizeAndCurrentTime() {

        Instant expectedNow =
                Instant.parse(
                        "2026-08-12T20:00:00Z"
                );

        when(
                outboxRecoveryService.recoverExpiredEvents(
                        10,
                        expectedNow
                )
        ).thenReturn(2);

        outboxRecoveryWorker.recoverExpiredEvents();

        verify(
                outboxRecoveryService
        ).recoverExpiredEvents(
                10,
                expectedNow
        );
    }
}