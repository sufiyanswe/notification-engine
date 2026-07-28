package com.portfolio.notification.api.request;

import jakarta.validation.constraints.NotBlank;

public record CreateNotificationRequest(

        @NotBlank
        String recipientId,

        String title,

        @NotBlank
        String message

) {
}