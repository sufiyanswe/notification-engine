package com.portfolio.notification.application.worker;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.port.NotificationChannel;
import com.portfolio.notification.domain.repository.NotificationRepository;
import com.portfolio.notification.domain.repository.OutboxRepository;
import com.portfolio.notification.infrastructure.delivery.NotificationChannelResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationChannelResolver notificationChannelResolver;

    public OutboxProcessor(
            OutboxRepository outboxRepository,
            NotificationRepository notificationRepository,
            NotificationChannelResolver notificationChannelResolver
    ) {
        this.outboxRepository = outboxRepository;
        this.notificationRepository = notificationRepository;
        this.notificationChannelResolver = notificationChannelResolver;
    }

    @Transactional
    public void process(UUID outboxEventId) {

        OutboxEvent outboxEvent =
                loadOutboxEvent(outboxEventId);

        Notification notification =
                loadNotification(outboxEvent);

        outboxEvent.markAsProcessing();

        NotificationChannel channel =
                notificationChannelResolver.resolve(
                        notification.getDeliveryChannel()
                );

        DeliveryResult result =
                channel.deliver(notification);

        if (result.successful()) {

            notification.markAsSent();

            outboxEvent.markAsProcessed();

        } else {

            notification.markAsFailed(
                    result.reason()
            );

            outboxEvent.markAsFailed();
        }
    }

    private OutboxEvent loadOutboxEvent(UUID outboxEventId) {

        return outboxRepository
                .findById(outboxEventId)
                .orElseThrow(() ->
                        new IllegalStateException(
                                "OutboxEvent not found: " + outboxEventId
                        )
                );
    }

    private Notification loadNotification(
            OutboxEvent outboxEvent
    ) {

        return notificationRepository
                .findById(outboxEvent.getNotificationId())
                .orElseThrow(() ->
                        new IllegalStateException(
                                "Notification not found: "
                                        + outboxEvent.getNotificationId()
                        )
                );
    }
}