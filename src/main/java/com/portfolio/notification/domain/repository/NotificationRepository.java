package com.portfolio.notification.domain.repository;

import com.portfolio.notification.domain.model.Notification;

import java.util.Optional;
import java.util.UUID;

public interface NotificationRepository {

    Notification save(Notification notification);
    Optional<Notification> findById(UUID id);


}