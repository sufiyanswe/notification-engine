package com.portfolio.notification.domain.repository;

import com.portfolio.notification.domain.model.Notification;

public interface NotificationRepository {

    Notification save(Notification notification);

}