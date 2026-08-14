package com.portfolio.notification.application.retry;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.model.OutboxEventType;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class ExponentialBackoffRetryPolicyTest {

    private static final Instant NOW =
            Instant.parse("2026-08-10T18:00:00Z");

    private static final Instant LEASE_UNTIL =
            Instant.parse("2026-08-10T18:00:30Z");

    private static final Clock CLOCK =
            Clock.fixed(
                    NOW,
                    ZoneOffset.UTC
            );

    private static final RetryPolicy POLICY =
            new ExponentialBackoffRetryPolicy(
                    5,
                    1000,
                    2.0,
                    CLOCK
            );

    @Test
    void shouldScheduleFirstRetryAfterOneSecond() {

        OutboxEvent event =
                new OutboxEvent(
                        java.util.UUID.randomUUID(),
                        OutboxEventType.DELIVER_NOTIFICATION
                );

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        Optional<Instant> nextAttemptAt =
                POLICY.nextAttemptAt(
                        event,
                        result
                );

        assertTrue(nextAttemptAt.isPresent());

        assertEquals(
                NOW.plusSeconds(1),
                nextAttemptAt.get()
        );
    }

    @Test
    void shouldScheduleSecondRetryAfterTwoSeconds() {

        OutboxEvent event =
                eventWithRetryCount(1);

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        Optional<Instant> nextAttemptAt =
                POLICY.nextAttemptAt(
                        event,
                        result
                );

        assertTrue(nextAttemptAt.isPresent());

        assertEquals(
                NOW.plusSeconds(2),
                nextAttemptAt.get()
        );
    }

    @Test
    void shouldScheduleThirdRetryAfterFourSeconds() {

        OutboxEvent event =
                eventWithRetryCount(2);

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        Optional<Instant> nextAttemptAt =
                POLICY.nextAttemptAt(
                        event,
                        result
                );

        assertTrue(nextAttemptAt.isPresent());

        assertEquals(
                NOW.plusSeconds(4),
                nextAttemptAt.get()
        );
    }

    @Test
    void shouldNotRetryWhenRetryBudgetIsExhausted() {

        OutboxEvent event =
                eventWithRetryCount(5);

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        Optional<Instant> nextAttemptAt =
                POLICY.nextAttemptAt(
                        event,
                        result
                );

        assertTrue(nextAttemptAt.isEmpty());
    }

    @Test
    void shouldNotRetryPermanentFailure() {

        OutboxEvent event =
                new OutboxEvent(
                        java.util.UUID.randomUUID(),
                        OutboxEventType.DELIVER_NOTIFICATION
                );

        DeliveryResult result =
                DeliveryResult.permanentFailure(
                        "Invalid recipient"
                );

        Optional<Instant> nextAttemptAt =
                POLICY.nextAttemptAt(
                        event,
                        result
                );

        assertTrue(nextAttemptAt.isEmpty());
    }
    @Test
    void shouldScheduleFourthRetryAfterEightSeconds() {

        OutboxEvent event =
                eventWithRetryCount(3);

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        Optional<Instant> nextAttemptAt =
                POLICY.nextAttemptAt(
                        event,
                        result
                );

        assertTrue(nextAttemptAt.isPresent());

        assertEquals(
                NOW.plusSeconds(8),
                nextAttemptAt.get()
        );
    }
    @Test
    void shouldScheduleFifthRetryAfterSixteenSeconds() {

        OutboxEvent event =
                eventWithRetryCount(4);

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        Optional<Instant> nextAttemptAt =
                POLICY.nextAttemptAt(
                        event,
                        result
                );

        assertTrue(nextAttemptAt.isPresent());

        assertEquals(
                NOW.plusSeconds(16),
                nextAttemptAt.get()
        );
    }

    private OutboxEvent eventWithRetryCount(
            int retryCount
    ) {

        OutboxEvent event =
                new OutboxEvent(
                        java.util.UUID.randomUUID(),
                        OutboxEventType.DELIVER_NOTIFICATION
                );

        for (int i = 0; i < retryCount; i++) {

            event.markAsProcessing(
                    LEASE_UNTIL
            );

            event.markForRetry(
                    NOW,
                    "Previous transient failure"
            );
        }

        return event;
    }
}