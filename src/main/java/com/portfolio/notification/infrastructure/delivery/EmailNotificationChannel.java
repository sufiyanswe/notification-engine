package com.portfolio.notification.infrastructure.delivery;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.NotificationChannelType;
import com.portfolio.notification.domain.port.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationChannel implements NotificationChannel {

    @Override
    public NotificationChannelType supports() {
        return NotificationChannelType.EMAIL;
    }

    @Override
    public DeliveryResult deliver(Notification notification) {

        if ("fail@example.com".equals(notification.getRecipientId())) {
            return new DeliveryResult(
                    false,
                    "SMTP server unavailable"
            );
        }

        return new DeliveryResult(
                true,
                null
        );
    }
}