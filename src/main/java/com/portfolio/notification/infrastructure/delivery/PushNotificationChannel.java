package com.portfolio.notification.infrastructure.delivery;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.NotificationChannelType;
import com.portfolio.notification.domain.port.NotificationChannel;
import org.springframework.stereotype.Component;

@Component
public class PushNotificationChannel implements NotificationChannel {

    @Override
    public NotificationChannelType supports() {
        return NotificationChannelType.PUSH;
    }

    @Override
    public DeliveryResult deliver(Notification notification) {

        if ("fail-device".equals(notification.getRecipientId())) {
            return DeliveryResult.transientFailure(
                    "Push provider unavailable"
            );
        }

        return DeliveryResult.success();
    }
}