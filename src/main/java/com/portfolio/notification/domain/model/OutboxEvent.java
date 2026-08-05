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

    protected OutboxEvent() {
        // Required by JPA
    }

    public OutboxEvent(UUID notificationId, OutboxEventType eventType) {
        if (notificationId == null) {
            throw new IllegalArgumentException("notificationId must not be null.");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType must not be null.");
        }

        this.notificationId = notificationId;
        this.eventType = eventType;
        this.status = OutboxStatus.PENDING;
        this.createdAt = Instant.now();
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

    public void markAsProcessing() {

        if (status != OutboxStatus.PENDING) {
            throw new IllegalStateException(
                    "OutboxEvent can only be marked as PROCESSING from PENDING. Current status: "
                            + status
            );
        }

        status = OutboxStatus.PROCESSING;
    }

    public void markAsProcessed() {

        if (status != OutboxStatus.PROCESSING) {
            throw new IllegalStateException(
                    "OutboxEvent can only be marked as PROCESSED from PROCESSING. Current status: "
                            + status
            );
        }

        status = OutboxStatus.PROCESSED;
        processedAt = Instant.now();
    }
}