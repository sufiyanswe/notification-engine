package com.portfolio.notification.application;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.NotificationChannelType;
import com.portfolio.notification.domain.port.NotificationChannel;
import com.portfolio.notification.domain.repository.NotificationRepository;
import com.portfolio.notification.infrastructure.delivery.NotificationChannelResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final NotificationChannelResolver notificationChannelResolver;

    public NotificationService(
            NotificationRepository notificationRepository,
            NotificationChannelResolver notificationChannelResolver
    ) {
        this.notificationRepository = notificationRepository;
        this.notificationChannelResolver = notificationChannelResolver;
    }

    @Transactional
    public Notification createNotification(
            String recipientId,
            String title,
            String message,
            NotificationChannelType deliveryChannel
    ) {

        Notification notification =
                new Notification(
                        recipientId,
                        title,
                        message,
                        deliveryChannel
                );

        notificationRepository.save(notification);

        NotificationChannel channel =
                notificationChannelResolver.resolve(
                        notification.getDeliveryChannel()
                );

        DeliveryResult result =
                channel.deliver(notification);

        if (result.successful()) {
            notification.markAsSent();
        } else {
            notification.markAsFailed(result.reason());
        }

        return notification;
    }
}