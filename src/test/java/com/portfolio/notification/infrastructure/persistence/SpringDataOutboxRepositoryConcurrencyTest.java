package com.portfolio.notification.infrastructure.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
class SpringDataOutboxRepositoryConcurrencyTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine");

    @Autowired
    private SpringDataOutboxRepository outboxRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PlatformTransactionManager transactionManager;

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
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldSkipLockedEventWhenAnotherTransactionAlreadyClaimedIt()
            throws Exception {

        UUID notificationId =
                UUID.randomUUID();

        UUID outboxEventId =
                UUID.randomUUID();

        Instant now =
                Instant.now();

        insertNotification(
                notificationId,
                now
        );

        insertOutboxEvent(
                outboxEventId,
                notificationId,
                now
        );

        CountDownLatch firstTransactionLocked =
                new CountDownLatch(1);

        CountDownLatch releaseFirstTransaction =
                new CountDownLatch(1);

        AtomicReference<List<?>> firstResult =
                new AtomicReference<>();

        AtomicReference<List<?>> secondResult =
                new AtomicReference<>();

        AtomicReference<Throwable> firstFailure =
                new AtomicReference<>();

        AtomicReference<Throwable> secondFailure =
                new AtomicReference<>();

        Thread firstTransaction =
                new Thread(() -> {

                    try {

                        TransactionTemplate transactionTemplate =
                                new TransactionTemplate(
                                        transactionManager
                                );

                        transactionTemplate.execute(status -> {

                            List<?> events =
                                    outboxRepository
                                            .findPendingForClaim(
                                                    1,
                                                    now
                                            );

                            firstResult.set(events);

                            assertEquals(
                                    1,
                                    events.size()
                            );

                            /*
                             * At this point PostgreSQL has acquired
                             * a row-level lock because the query uses:
                             *
                             * FOR UPDATE SKIP LOCKED
                             *
                             * The transaction deliberately remains open.
                             */
                            firstTransactionLocked.countDown();

                            try {

                                assertTrue(
                                        releaseFirstTransaction.await(
                                                10,
                                                TimeUnit.SECONDS
                                        )
                                );

                            } catch (InterruptedException ex) {

                                Thread.currentThread().interrupt();

                                throw new IllegalStateException(
                                        "First transaction interrupted.",
                                        ex
                                );
                            }

                            return null;
                        });

                    } catch (Throwable ex) {

                        firstFailure.set(ex);
                    }
                });

        Thread secondTransaction =
                new Thread(() -> {

                    try {

                        /*
                         * Do not start transaction B until transaction A
                         * has definitely acquired the row lock.
                         */
                        assertTrue(
                                firstTransactionLocked.await(
                                        10,
                                        TimeUnit.SECONDS
                                )
                        );

                        TransactionTemplate transactionTemplate =
                                new TransactionTemplate(
                                        transactionManager
                                );

                        transactionTemplate.execute(status -> {

                            List<?> events =
                                    outboxRepository
                                            .findPendingForClaim(
                                                    1,
                                                    now
                                            );

                            secondResult.set(events);

                            return null;
                        });

                    } catch (Throwable ex) {

                        secondFailure.set(ex);
                    }
                });

        firstTransaction.start();
        secondTransaction.start();

        /*
         * Transaction B should finish without waiting for transaction A,
         * because SKIP LOCKED tells PostgreSQL to skip the locked row.
         */
        secondTransaction.join(
                TimeUnit.SECONDS.toMillis(10)
        );

        /*
         * Allow transaction A to release its lock.
         */
        releaseFirstTransaction.countDown();

        firstTransaction.join(
                TimeUnit.SECONDS.toMillis(10)
        );

        if (firstFailure.get() != null) {

            throw new AssertionError(
                    "First transaction failed.",
                    firstFailure.get()
            );
        }

        if (secondFailure.get() != null) {

            throw new AssertionError(
                    "Second transaction failed.",
                    secondFailure.get()
            );
        }

        /*
         * Transaction A successfully claimed the event.
         */
        assertEquals(
                1,
                firstResult.get().size()
        );

        /*
         * Transaction B encountered the same pending event,
         * but PostgreSQL skipped it because transaction A
         * was holding the row lock.
         */
        assertTrue(
                secondResult.get().isEmpty()
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
                "concurrency-test@example.com",
                "Concurrency Test",
                "Testing SKIP LOCKED",
                "EMAIL",
                "PENDING",
                Timestamp.from(createdAt)
        );
    }

    private void insertOutboxEvent(
            UUID outboxEventId,
            UUID notificationId,
            Instant createdAt
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
                    ?
                )
                """,
                outboxEventId,
                notificationId,
                "DELIVER_NOTIFICATION",
                "PENDING",
                Timestamp.from(createdAt),
                null,
                0,
                Timestamp.from(createdAt),
                null
        );
    }
}