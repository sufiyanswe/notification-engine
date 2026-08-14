package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface SpringDataOutboxRepository
        extends JpaRepository<OutboxEvent, UUID> {

    @Query(
            value = """
                    SELECT *
                    FROM outbox_events
                    WHERE status = 'PENDING'
                      AND next_attempt_at <= :now
                    ORDER BY created_at ASC
                    LIMIT :limit
                    FOR UPDATE SKIP LOCKED
                    """,
            nativeQuery = true
    )
    List<OutboxEvent> findPendingForClaim(
            @Param("limit") int limit,
            @Param("now") Instant now
    );

    @Query(
            value = """
                SELECT *
                FROM outbox_events
                WHERE status = 'PROCESSING'
                  AND lease_until <= :now
                ORDER BY created_at ASC
                LIMIT :limit
                FOR UPDATE SKIP LOCKED
                """,
            nativeQuery = true
    )
    List<OutboxEvent> findExpiredProcessingForRecovery(
            @Param("limit") int limit,
            @Param("now") Instant now
    );
}