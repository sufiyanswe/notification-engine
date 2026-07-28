package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Repository;

@Repository
public class NotificationRepositoryImpl
        implements NotificationRepository {

    private final SpringDataNotificationRepository repository;

    public NotificationRepositoryImpl(
            SpringDataNotificationRepository repository
    ) {
        this.repository = repository;
    }

    @Override
    public Notification save(Notification notification) {
        return repository.save(notification);
    }
}