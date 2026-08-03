package com.portfolio.notification.api.request;

import com.portfolio.notification.domain.model.NotificationChannelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateNotificationRequest(

        @NotBlank
        String recipientId,

        String title,

        @NotBlank
        String message,

        @NotNull
        NotificationChannelType deliveryChannel


) {
}