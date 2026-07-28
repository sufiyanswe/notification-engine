package com.portfolio.notification.domain.model;

import jakarta.persistence.*;

import org.hibernate.annotations.UuidGenerator;

import java.time.Instant;
import java.util.UUID;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "notifications")
public class Notification {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId;

    @Column(name = "title")
    private String title;

    @Column(name = "message", nullable = false)
    private String message;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
    }

    public Notification(
            String recipientId,
            String title,
            String message
    ) {
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.status = NotificationStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}