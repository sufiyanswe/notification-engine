package com.portfolio.notification.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
class SpringDataOutboxRepositoryRecoveryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SpringDataOutboxRepository outboxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @AfterEach
    void cleanDatabase() {

        jdbcTemplate.execute(
                "DELETE FROM outbox_events"
        );

        jdbcTemplate.execute(
                "DELETE FROM notifications"
        );
    }

    @Test
    void shouldFindExpiredProcessingEvents() {

        Instant now =
                Instant.parse(
                        "2026-08-12T20:00:00Z"
                );

        UUID expiredNotificationId =
                UUID.randomUUID();

        UUID expiredEventId =
                UUID.randomUUID();

        UUID activeNotificationId =
                UUID.randomUUID();

        UUID activeEventId =
                UUID.randomUUID();

        insertNotification(
                expiredNotificationId,
                now
        );

        insertOutboxEvent(
                expiredEventId,
                expiredNotificationId,
                now,
                "PROCESSING",
                now.minusSeconds(10)
        );

        insertNotification(
                activeNotificationId,
                now
        );

        insertOutboxEvent(
                activeEventId,
                activeNotificationId,
                now,
                "PROCESSING",
                now.plusSeconds(30)
        );

        List<?> events =
                outboxRepository.findExpiredProcessingForRecovery(
                        10,
                        now
                );

        assertEquals(
                1,
                events.size()
        );
    }

    @Test
    void shouldIgnorePendingEvents() {

        Instant now =
                Instant.parse(
                        "2026-08-12T20:00:00Z"
                );

        UUID notificationId =
                UUID.randomUUID();

        UUID eventId =
                UUID.randomUUID();

        insertNotification(
                notificationId,
                now
        );

        insertOutboxEvent(
                eventId,
                notificationId,
                now,
                "PENDING",
                now.minusSeconds(10)
        );

        List<?> events =
                outboxRepository.findExpiredProcessingForRecovery(
                        10,
                        now
                );

        assertTrue(
                events.isEmpty()
        );
    }

    @Test
    void shouldRespectRequestedLimit() {

        Instant now =
                Instant.parse(
                        "2026-08-12T20:00:00Z"
                );

        for (int i = 0; i < 3; i++) {

            UUID notificationId =
                    UUID.randomUUID();

            UUID eventId =
                    UUID.randomUUID();

            insertNotification(
                    notificationId,
                    now
            );

            insertOutboxEvent(
                    eventId,
                    notificationId,
                    now.plusSeconds(i),
                    "PROCESSING",
                    now.minusSeconds(10)
            );
        }

        List<?> events =
                outboxRepository.findExpiredProcessingForRecovery(
                        2,
                        now
                );

        assertEquals(
                2,
                events.size()
        );
    }

    private void insertNotification(
            UUID notificationId,
            Instant createdAt
    ) {

        jdbcTemplate.update(
                """
                INSERT INTO notifications (
                    id,
                    recipient_id,
                    title,
                    message,
                    delivery_channel,
                    status,
                    created_at
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
                """,
                notificationId,
                "recovery-test@example.com",
                "Recovery Test",
                "Testing expired lease recovery",
                "EMAIL",
                "PENDING",
                Timestamp.from(createdAt)
        );
    }

    private void insertOutboxEvent(
            UUID outboxEventId,
            UUID notificationId,
            Instant createdAt,
            String status,
            Instant leaseUntil
    ) {

        jdbcTemplate.update(
                """
                INSERT INTO outbox_events (
                    id,
                    notification_id,
                    event_type,
                    status,
                    created_at,
                    processed_at,
                    retry_count,
                    next_attempt_at,
                    lease_until,
                    last_failure_reason
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    ?
                )
                """,
                outboxEventId,
                notificationId,
                "DELIVER_NOTIFICATION",
                status,
                Timestamp.from(createdAt),
                null,
                0,
                Timestamp.from(createdAt),
                Timestamp.from(leaseUntil),
                null
        );
    }
}