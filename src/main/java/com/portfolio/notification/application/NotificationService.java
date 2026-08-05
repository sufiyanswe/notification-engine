package com.portfolio.notification.application;

import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.NotificationChannelType;
import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.model.OutboxEventType;
import com.portfolio.notification.domain.repository.NotificationRepository;
import com.portfolio.notification.domain.repository.OutboxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final OutboxRepository outboxRepository;

    public NotificationService(
            NotificationRepository notificationRepository,
            OutboxRepository outboxRepository
    ) {
        this.notificationRepository = notificationRepository;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public Notification createNotification(
            String recipientId,
            String title,
            String message,
            NotificationChannelType deliveryChannel
    ) {

        Notification notification =
                notificationRepository.save(
                        new Notification(
                                recipientId,
                                title,
                                message,
                                deliveryChannel
                        )
                );

        OutboxEvent outboxEvent =
                new OutboxEvent(
                        notification.getId(),
                        OutboxEventType.DELIVER_NOTIFICATION
                );

        outboxRepository.save(outboxEvent);

        // TEMPORARY: Force transaction rollback
        return notification;
    }
}