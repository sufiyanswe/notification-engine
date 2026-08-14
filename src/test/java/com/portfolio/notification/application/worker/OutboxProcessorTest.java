package com.portfolio.notification.application.worker;

import com.portfolio.notification.application.retry.RetryPolicy;
import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.NotificationChannelType;
import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.model.OutboxEventType;
import com.portfolio.notification.domain.model.OutboxStatus;
import com.portfolio.notification.domain.model.NotificationStatus;
import com.portfolio.notification.domain.port.NotificationChannel;
import com.portfolio.notification.domain.repository.NotificationRepository;
import com.portfolio.notification.domain.repository.OutboxRepository;
import com.portfolio.notification.infrastructure.delivery.NotificationChannelResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OutboxProcessorTest {

    private static final UUID OUTBOX_EVENT_ID =
            UUID.randomUUID();

    private static final UUID NOTIFICATION_ID =
            UUID.randomUUID();

    private static final Instant NEXT_ATTEMPT_AT =
            Instant.parse("2026-08-10T18:00:05Z");

    private static final Instant LEASE_UNTIL =
            Instant.parse("2026-08-10T18:00:30Z");

    private OutboxRepository outboxRepository;
    private NotificationRepository notificationRepository;
    private NotificationChannelResolver notificationChannelResolver;
    private RetryPolicy retryPolicy;
    private NotificationChannel notificationChannel;

    private OutboxProcessor processor;

    @BeforeEach
    void setUp() {

        outboxRepository =
                mock(OutboxRepository.class);

        notificationRepository =
                mock(NotificationRepository.class);

        notificationChannelResolver =
                mock(NotificationChannelResolver.class);

        retryPolicy =
                mock(RetryPolicy.class);

        notificationChannel =
                mock(NotificationChannel.class);

        processor =
                new OutboxProcessor(
                        outboxRepository,
                        notificationRepository,
                        notificationChannelResolver,
                        retryPolicy
                );
    }

    @Test
    void shouldMarkNotificationSentAndOutboxProcessedOnSuccessfulDelivery() {

        OutboxEvent event = createEvent();

        Notification notification =
                createNotification();

        DeliveryResult result =
                DeliveryResult.success();

        givenOutboxEvent(event);
        givenNotification(notification);

        when(
                notificationChannelResolver.resolve(
                        NotificationChannelType.EMAIL
                )
        ).thenReturn(notificationChannel);

        when(
                notificationChannel.deliver(
                        notification,
                        OUTBOX_EVENT_ID
                )
        ).thenReturn(result);

        processor.process(OUTBOX_EVENT_ID);

        assertEquals(
                NotificationStatus.SENT,
                notification.getStatus()
        );

        assertEquals(
                OutboxStatus.PROCESSED,
                event.getStatus()
        );

        assertNotNull(
                event.getProcessedAt()
        );

        verify(retryPolicy, never())
                .nextAttemptAt(event, result);

        verify(notificationChannel)
                .deliver(
                        notification,
                        OUTBOX_EVENT_ID
                );
    }

    @Test
    void shouldMarkOutboxProcessedWithoutRedeliveryWhenNotificationIsAlreadySent() {

        OutboxEvent event = createEvent();

        Notification notification =
                createNotification();

        notification.markAsSent();

        givenOutboxEvent(event);
        givenNotification(notification);

        processor.process(OUTBOX_EVENT_ID);

        assertEquals(
                NotificationStatus.SENT,
                notification.getStatus()
        );

        assertEquals(
                OutboxStatus.PROCESSED,
                event.getStatus()
        );

        verifyNoInteractions(notificationChannelResolver);
        verifyNoInteractions(notificationChannel);
        verifyNoInteractions(retryPolicy);
    }

    @Test
    void shouldScheduleRetryAndKeepNotificationPendingOnTransientFailure() {

        OutboxEvent event = createEvent();

        Notification notification =
                createNotification();

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        givenOutboxEvent(event);
        givenNotification(notification);

        when(
                notificationChannelResolver.resolve(
                        NotificationChannelType.EMAIL
                )
        ).thenReturn(notificationChannel);

        when(
                notificationChannel.deliver(
                        notification,
                        OUTBOX_EVENT_ID
                )
        ).thenReturn(result);

        when(
                retryPolicy.nextAttemptAt(
                        event,
                        result
                )
        ).thenReturn(
                Optional.of(NEXT_ATTEMPT_AT)
        );

        processor.process(OUTBOX_EVENT_ID);

        assertEquals(
                NotificationStatus.PENDING,
                notification.getStatus()
        );

        assertEquals(
                OutboxStatus.PENDING,
                event.getStatus()
        );

        assertEquals(
                1,
                event.getRetryCount()
        );

        assertEquals(
                NEXT_ATTEMPT_AT,
                event.getNextAttemptAt()
        );

        assertEquals(
                "SMTP server unavailable",
                event.getLastFailureReason()
        );

        assertNull(
                notification.getFailureReason()
        );

        verify(retryPolicy)
                .nextAttemptAt(event, result);
    }

    @Test
    void shouldMarkNotificationAndOutboxFailedWhenRetryIsExhausted() {

        OutboxEvent event = createEvent();

        Notification notification =
                createNotification();

        DeliveryResult result =
                DeliveryResult.transientFailure(
                        "SMTP server unavailable"
                );

        givenOutboxEvent(event);
        givenNotification(notification);

        when(
                notificationChannelResolver.resolve(
                        NotificationChannelType.EMAIL
                )
        ).thenReturn(notificationChannel);

        when(
                notificationChannel.deliver(
                        notification,
                        OUTBOX_EVENT_ID
                )
        ).thenReturn(result);

        when(
                retryPolicy.nextAttemptAt(
                        event,
                        result
                )
        ).thenReturn(
                Optional.empty()
        );

        processor.process(OUTBOX_EVENT_ID);

        assertEquals(
                NotificationStatus.FAILED,
                notification.getStatus()
        );

        assertEquals(
                "SMTP server unavailable",
                notification.getFailureReason()
        );

        assertEquals(
                OutboxStatus.FAILED,
                event.getStatus()
        );

        assertEquals(
                "SMTP server unavailable",
                event.getLastFailureReason()
        );

        assertNotNull(
                event.getProcessedAt()
        );
    }

    @Test
    void shouldMarkNotificationAndOutboxFailedForPermanentFailure() {

        OutboxEvent event = createEvent();

        Notification notification =
                createNotification();

        DeliveryResult result =
                DeliveryResult.permanentFailure(
                        "Invalid recipient"
                );

        givenOutboxEvent(event);
        givenNotification(notification);

        when(
                notificationChannelResolver.resolve(
                        NotificationChannelType.EMAIL
                )
        ).thenReturn(notificationChannel);

        when(
                notificationChannel.deliver(
                        notification,
                        OUTBOX_EVENT_ID
                )
        ).thenReturn(result);

        when(
                retryPolicy.nextAttemptAt(
                        event,
                        result
                )
        ).thenReturn(
                Optional.empty()
        );

        processor.process(OUTBOX_EVENT_ID);

        assertEquals(
                NotificationStatus.FAILED,
                notification.getStatus()
        );

        assertEquals(
                "Invalid recipient",
                notification.getFailureReason()
        );

        assertEquals(
                OutboxStatus.FAILED,
                event.getStatus()
        );

        assertEquals(
                "Invalid recipient",
                event.getLastFailureReason()
        );

        verify(retryPolicy)
                .nextAttemptAt(event, result);
    }

    private OutboxEvent createEvent() {

        OutboxEvent event =
                new OutboxEvent(
                        NOTIFICATION_ID,
                        OutboxEventType.DELIVER_NOTIFICATION
                );
        event.markAsProcessing(LEASE_UNTIL);

        return event;
    }

    private Notification createNotification() {

        return new Notification(
                "recipient@example.com",
                "Test notification",
                "Test message",
                NotificationChannelType.EMAIL
        );
    }

    private void givenOutboxEvent(
            OutboxEvent event
    ) {

        when(
                outboxRepository.findById(
                        OUTBOX_EVENT_ID
                )
        ).thenReturn(
                Optional.of(event)
        );
    }

    private void givenNotification(
            Notification notification
    ) {

        when(
                notificationRepository.findById(
                        NOTIFICATION_ID
                )
        ).thenReturn(
                Optional.of(notification)
        );
    }
}