package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.Notification;
import com.portfolio.notification.domain.repository.NotificationRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public class NotificationRepositoryImpl
        implements NotificationRepository {

    private final SpringDataNotificationRepository springDataRepository;

    public NotificationRepositoryImpl(
            SpringDataNotificationRepository springDataRepository
    ) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return springDataRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return springDataRepository.findById(id);
    }
}