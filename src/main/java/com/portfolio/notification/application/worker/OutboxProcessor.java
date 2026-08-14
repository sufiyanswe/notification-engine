package com.portfolio.notification.application.worker;

import com.portfolio.notification.application.retry.RetryPolicy;
import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.port.NotificationChannel;
import com.portfolio.notification.domain.repository.NotificationRepository;
import com.portfolio.notification.domain.repository.OutboxRepository;
import com.portfolio.notification.infrastructure.delivery.NotificationChannelResolver;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class OutboxProcessor {

    private final OutboxRepository outboxRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationChannelResolver notificationChannelResolver;
    private final RetryPolicy retryPolicy;

    public OutboxProcessor(
            OutboxRepository outboxRepository,
            NotificationRepository notificationRepository,
            NotificationChannelResolver notificationChannelResolver,
            RetryPolicy retryPolicy
    ) {
        this.outboxRepository = outboxRepository;
        this.notificationRepository = notificationRepository;
        this.notificationChannelResolver = notificationChannelResolver;
        this.retryPolicy = retryPolicy;
    }

    @Transactional
    public void process(UUID outboxEventId) {

        OutboxEvent outboxEvent =
                loadOutboxEvent(outboxEventId);

        Notification notification =
                loadNotification(outboxEvent);

        NotificationChannel channel =
                notificationChannelResolver.resolve(
                        notification.getDeliveryChannel()
                );

        DeliveryResult result =
                channel.deliver(notification);

        if (result.successful()) {

            notification.markAsSent();
            outboxEvent.markAsProcessed();

            return;
        }

        Optional<Instant> nextAttemptAt =
                retryPolicy.nextAttemptAt(
                        outboxEvent,
                        result
                );

        if (nextAttemptAt.isPresent()) {

            outboxEvent.markForRetry(
                    nextAttemptAt.get(),
                    result.reason()
            );

            return;
        }

        notification.markAsFailed(
                result.reason()
        );

        outboxEvent.markAsFailed(
                result.reason()
        );
    }

    private OutboxEvent loadOutboxEvent(
            UUID outboxEventId
    ) {

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