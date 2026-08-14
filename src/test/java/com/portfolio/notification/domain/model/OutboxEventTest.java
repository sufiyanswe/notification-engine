package com.portfolio.notification.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OutboxEventTest {

    private static final UUID NOTIFICATION_ID =
            UUID.randomUUID();

    private static final Instant LEASE_UNTIL =
            Instant.parse("2026-08-10T18:00:30Z");

    private static final Instant NEXT_ATTEMPT =
            Instant.parse("2026-08-10T18:00:05Z");

    @Test
    void shouldStartAsPending() {

        OutboxEvent event =
                new OutboxEvent(
                        NOTIFICATION_ID,
                        OutboxEventType.DELIVER_NOTIFICATION
                );

        assertEquals(
                OutboxStatus.PENDING,
                event.getStatus()
        );

        assertEquals(
                0,
                event.getRetryCount()
        );

        assertNotNull(
                event.getNextAttemptAt()
        );

        assertNull(
                event.getLastFailureReason()
        );

        assertNull(
                event.getLeaseUntil()
        );
    }

    @Test
    void shouldTransitionFromPendingToProcessing() {

        OutboxEvent event = createEvent();

        event.markAsProcessing(
                LEASE_UNTIL
        );

        assertEquals(
                OutboxStatus.PROCESSING,
                event.getStatus()
        );

        assertEquals(
                LEASE_UNTIL,
                event.getLeaseUntil()
        );
    }

    @Test
    void shouldScheduleRetryFromProcessing() {

        OutboxEvent event = createEvent();

        event.markAsProcessing(
                LEASE_UNTIL
        );

        event.markForRetry(
                NEXT_ATTEMPT,
                "SMTP server unavailable"
        );

        assertEquals(
                OutboxStatus.PENDING,
                event.getStatus()
        );

        assertEquals(
                1,
                event.getRetryCount()
        );

        assertEquals(
                NEXT_ATTEMPT,
                event.getNextAttemptAt()
        );

        assertEquals(
                "SMTP server unavailable",
                event.getLastFailureReason()
        );

        assertNull(
                event.getProcessedAt()
        );

        assertNull(
                event.getLeaseUntil()
        );
    }

    @Test
    void shouldIncrementRetryCountOnEveryRetry() {

        OutboxEvent event = createEvent();

        event.markAsProcessing(
                LEASE_UNTIL
        );

        event.markForRetry(
                NEXT_ATTEMPT,
                "First failure"
        );

        event.markAsProcessing(
                LEASE_UNTIL
        );

        event.markForRetry(
                NEXT_ATTEMPT.plusSeconds(2),
                "Second failure"
        );

        assertEquals(
                2,
                event.getRetryCount()
        );

        assertEquals(
                "Second failure",
                event.getLastFailureReason()
        );

        assertNull(
                event.getLeaseUntil()
        );
    }

    @Test
    void shouldTransitionFromProcessingToProcessed() {

        OutboxEvent event = createEvent();

        event.markAsProcessing(
                LEASE_UNTIL
        );

        event.markAsProcessed();

        assertEquals(
                OutboxStatus.PROCESSED,
                event.getStatus()
        );

        assertNotNull(
                event.getProcessedAt()
        );

        assertNull(
                event.getLastFailureReason()
        );

        assertNull(
                event.getLeaseUntil()
        );
    }

    @Test
    void shouldTransitionFromProcessingToFailed() {

        OutboxEvent event = createEvent();

        event.markAsProcessing(
                LEASE_UNTIL
        );

        event.markAsFailed(
                "Invalid recipient"
        );

        assertEquals(
                OutboxStatus.FAILED,
                event.getStatus()
        );

        assertNotNull(
                event.getProcessedAt()
        );

        assertEquals(
                "Invalid recipient",
                event.getLastFailureReason()
        );

        assertNull(
                event.getLeaseUntil()
        );
    }

    @Test
    void shouldRejectProcessingWhenAlreadyProcessing() {

        OutboxEvent event = createEvent();

        event.markAsProcessing(
                LEASE_UNTIL
        );

        assertThrows(
                IllegalStateException.class,
                () -> event.markAsProcessing(
                        LEASE_UNTIL
                )
        );
    }

    @Test
    void shouldRejectProcessingWhenAlreadyProcessed() {

        OutboxEvent event = createEvent();

        event.markAsProcessing(
                LEASE_UNTIL
        );

        event.markAsProcessed();

        assertThrows(
                IllegalStateException.class,
                () -> event.markAsProcessing(
                        LEASE_UNTIL
                )
        );
    }

    @Test
    void shouldRejectRetryWhenNotProcessing() {

        OutboxEvent event = createEvent();

        assertThrows(
                IllegalStateException.class,
                () -> event.markForRetry(
                        NEXT_ATTEMPT,
                        "Failure"
                )
        );
    }

    @Test
    void shouldRejectProcessingToProcessedWhenNotProcessing() {

        OutboxEvent event = createEvent();

        assertThrows(
                IllegalStateException.class,
                event::markAsProcessed
        );
    }

    @Test
    void shouldRejectFailureWhenNotProcessing() {

        OutboxEvent event = createEvent();

        assertThrows(
                IllegalStateException.class,
                () -> event.markAsFailed(
                        "Failure"
                )
        );
    }

    private OutboxEvent createEvent() {

        return new OutboxEvent(
                NOTIFICATION_ID,
                OutboxEventType.DELIVER_NOTIFICATION
        );
    }
}