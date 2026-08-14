package com.portfolio.notification.application.retry;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.OutboxEvent;

import java.time.Instant;
import java.util.Optional;

public interface RetryPolicy {
    Optional<Instant> nextAttemptAt(
            OutboxEvent event,
            DeliveryResult result
    );
}
