package com.portfolio.notification.api.controller;

import com.portfolio.notification.api.request.CreateNotificationRequest;
import com.portfolio.notification.api.response.NotificationResponse;
import com.portfolio.notification.application.NotificationService;
import com.portfolio.notification.domain.model.Notification;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(
            NotificationService notificationService
    ) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> createNotification(

            @Valid
            @RequestBody
            CreateNotificationRequest request

    ) {

        Notification notification =
                notificationService.createNotification(
                        request.recipientId(),
                        request.title(),
                        request.message(),
                        request.deliveryChannel()
                );

        NotificationResponse response =
                new NotificationResponse(
                        notification.getId(),
                        notification.getRecipientId(),
                        notification.getTitle(),
                        notification.getMessage(),
                        notification.getStatus(),
                        notification.getCreatedAt()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}