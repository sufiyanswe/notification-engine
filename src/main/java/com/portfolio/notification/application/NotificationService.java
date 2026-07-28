package com.portfolio.notification.application;

import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public NotificationService(
            NotificationRepository notificationRepository
    ) {
        this.notificationRepository = notificationRepository;
    }

    public Notification createNotification(
            String recipientId,
            String title,
            String message
    ) {

        Notification notification =
                new Notification(
                        recipientId,
                        title,
                        message
                );

        return notificationRepository.save(notification);

    }

}