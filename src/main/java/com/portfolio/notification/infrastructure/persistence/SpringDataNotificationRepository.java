package com.portfolio.notification.infrastructure.persistence;

import com.portfolio.notification.domain.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SpringDataNotificationRepository
        extends JpaRepository<Notification, UUID> {

}