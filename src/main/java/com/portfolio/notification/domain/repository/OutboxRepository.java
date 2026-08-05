package com.portfolio.notification.domain.repository;

import com.portfolio.notification.domain.model.OutboxEvent;

public interface OutboxRepository {

    OutboxEvent save(OutboxEvent outboxEvent);

}