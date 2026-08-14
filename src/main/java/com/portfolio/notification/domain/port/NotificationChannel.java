package com.portfolio.notification.domain.port;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.model.NotificationChannelType;
import java.util.UUID;
public interface NotificationChannel {
    NotificationChannelType supports();
    DeliveryResult deliver(Notification notification, UUID deliveryId);
}
