package com.portfolio.notification.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "notification_id", nullable = false)
    private UUID notificationId;

    @Column(name = "event_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxEventType eventType;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "next_attempt_at", nullable = false)
    private Instant nextAttemptAt;

    @Column(name = "lease_until")
    private Instant leaseUntil;

    @Column(name = "last_failure_reason")
    private String lastFailureReason;



    protected OutboxEvent() {
        // Required by JPA
    }

    public OutboxEvent(
            UUID notificationId,
            OutboxEventType eventType

    ) {

        if (notificationId == null) {
            throw new IllegalArgumentException(
                    "notificationId must not be null."
            );
        }

        if (eventType == null) {
            throw new IllegalArgumentException(
                    "eventType must not be null."
            );
        }

        this.notificationId = notificationId;
        this.eventType = eventType;
        this.status = OutboxStatus.PENDING;

        Instant now = Instant.now();
        this.createdAt = now;
        this.retryCount = 0;
        this.nextAttemptAt = now;


    }

    public UUID getId() {
        return id;
    }

    public UUID getNotificationId() {
        return notificationId;
    }

    public OutboxEventType getEventType() {
        return eventType;
    }

    public OutboxStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public Instant getNextAttemptAt() {
        return nextAttemptAt;
    }

    public Instant getLeaseUntil() {
        return leaseUntil;
    }

    public String getLastFailureReason() {
        return lastFailureReason;
    }


    public void markAsProcessing(Instant leaseUntil) {

        if (status != OutboxStatus.PENDING) {
            throw new IllegalStateException(
                    "Cannot transition OutboxEvent from "
                            + status
                            + " to PROCESSING."
            );
        }

        if (leaseUntil == null) {
            throw new IllegalArgumentException(
                    "leaseUntil must not be null."
            );
        }

        this.status = OutboxStatus.PROCESSING;
        this.leaseUntil = leaseUntil;
    }

    public void markForRetry(Instant nextAttemptAt, String reason) {
        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot transition OutboxEvent from " + status + " to PENDING for retry."
            );
        }
        if (nextAttemptAt == null) {
            throw new IllegalArgumentException(
                    "nextAttemptAt must not be null."
            );
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Failure reason must not be null or blank."
            );
        }
        this.status = OutboxStatus.PENDING;
        this.retryCount++;
        this.nextAttemptAt = nextAttemptAt;
        this.lastFailureReason = reason;
        this.leaseUntil = null;


    }

    public void recoverFromExpiredLease(Instant nextAttemptAt) {

        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot recover OutboxEvent from "
                            + status
                            + "."
            );
        }

        if (nextAttemptAt == null) {
            throw new IllegalArgumentException(
                    "nextAttemptAt must not be null."
            );
        }

        this.status = OutboxStatus.PENDING;
        this.nextAttemptAt = nextAttemptAt;
        this.leaseUntil = null;
    }

    public void markAsProcessed() {

        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot transition OutboxEvent from "
                            + status
                            + " to PROCESSED."
            );
        }

        this.status = OutboxStatus.PROCESSED;
        this.processedAt = Instant.now();
        this.lastFailureReason = null;
        this.leaseUntil = null;
    }

    public void markAsFailed(String reason) {

        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Cannot transition OutboxEvent from "
                            + status
                            + " to FAILED."
            );
        }

        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException(
                    "Failure reason must not be null or blank."
            );
        }

        this.status = OutboxStatus.FAILED;
        this.processedAt = Instant.now();
        this.lastFailureReason = reason;
        this.leaseUntil = null;
    }
}