package com.portfolio.notification.infrastructure.delivery;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.NotificationChannelType;
import com.portfolio.notification.domain.port.NotificationChannel;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public NotificationChannelType supports() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public DeliveryResult deliver(Notification notification, UUID deliveryId) {

        if ("fail@example.com".equals(notification.getRecipientId())) {
            return DeliveryResult.transientFailure(
                    "SMTP server unavailable"
            );
        }

        return DeliveryResult.success();
    }
}