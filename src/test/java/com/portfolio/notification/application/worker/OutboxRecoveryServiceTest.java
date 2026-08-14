package com.portfolio.notification.application.worker;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.repository.OutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxRecoveryServiceTest {

    private OutboxRepository outboxRepository;

    private OutboxRecoveryService outboxRecoveryService;

    @BeforeEach
    void setUp() {

        outboxRepository =
                mock(OutboxRepository.class);

        outboxRecoveryService =
                new OutboxRecoveryService(
                        outboxRepository
                );
    }

    @Test
    void shouldRecoverExpiredProcessingEvents() {

        Instant now =
                Instant.parse(
                        "2026-08-12T20:00:00Z"
                );

        OutboxEvent firstEvent =
                mock(OutboxEvent.class);

        OutboxEvent secondEvent =
                mock(OutboxEvent.class);

        when(
                outboxRepository.findExpiredProcessingForRecovery(
                        10,
                        now
                )
        ).thenReturn(
                List.of(
                        firstEvent,
                        secondEvent
                )
        );

        int recoveredCount =
                outboxRecoveryService.recoverExpiredEvents(
                        10,
                        now
                );

        assertEquals(
                2,
                recoveredCount
        );

        verify(firstEvent)
                .recoverFromExpiredLease(
                        now
                );

        verify(secondEvent)
                .recoverFromExpiredLease(
                        now
                );

        verify(
                outboxRepository
        ).findExpiredProcessingForRecovery(
                10,
                now
        );
    }

    @Test
    void shouldReturnZeroWhenNoExpiredEventsExist() {

        Instant now =
                Instant.parse(
                        "2026-08-12T20:00:00Z"
                );

        when(
                outboxRepository.findExpiredProcessingForRecovery(
                        10,
                        now
                )
        ).thenReturn(
                List.of()
        );

        int recoveredCount =
                outboxRecoveryService.recoverExpiredEvents(
                        10,
                        now
                );

        assertEquals(
                0,
                recoveredCount
        );

        verify(
                outboxRepository
        ).findExpiredProcessingForRecovery(
                10,
                now
        );
    }

    @Test
    void shouldPassRequestedLimitAndCurrentTimeToRepository() {

        int limit = 5;

        Instant now =
                Instant.parse(
                        "2026-08-12T20:05:00Z"
                );

        when(
                outboxRepository.findExpiredProcessingForRecovery(
                        limit,
                        now
                )
        ).thenReturn(
                List.of()
        );

        outboxRecoveryService.recoverExpiredEvents(
                limit,
                now
        );

        verify(
                outboxRepository
        ).findExpiredProcessingForRecovery(
                limit,
                now
        );
    }
}