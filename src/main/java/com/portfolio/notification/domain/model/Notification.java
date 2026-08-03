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

    @Column(name = "delivery_channel", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationChannelType deliveryChannel;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected Notification() {
        // Required by JPA
    }

    public Notification(
            String recipientId,
            String title,
            String message,
            NotificationChannelType deliveryChannel
    ) {
        this.recipientId = recipientId;
        this.title = title;
        this.message = message;
        this.deliveryChannel = deliveryChannel;
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

    public NotificationChannelType getDeliveryChannel() {
        return deliveryChannel;
    }

    public NotificationStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markAsSent() {
        if (status != NotificationStatus.PENDING) {
            throw new IllegalStateException("Notification can only be marked as SENT from PENDING. Current status: " + status);
        }
        status = NotificationStatus.SENT;
    }
}