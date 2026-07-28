package com.portfolio.notification.api.response;

import com.portfolio.notification.domain.model.NotificationStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationResponse(

        UUID id,

        String recipientId,

        String title,

        String message,

        NotificationStatus status,

        Instant createdAt

) {
}