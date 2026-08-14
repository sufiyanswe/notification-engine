package com.portfolio.notification.application.retry;

import com.portfolio.notification.domain.model.DeliveryResult;
import com.portfolio.notification.domain.model.FailureType;
import com.portfolio.notification.domain.model.OutboxEvent;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

public class ExponentialBackoffRetryPolicy
        implements RetryPolicy {

    private final int maxRetries;
    private final long initialDelayMillis;
    private final double multiplier;
    private final Clock clock;

    public ExponentialBackoffRetryPolicy(
            int maxRetries,
            long initialDelayMillis,
            double multiplier,
            Clock clock
    ) {
        if (maxRetries < 0) {
            throw new IllegalArgumentException(
                    "maxRetries must not be negative."
            );
        }

        if (initialDelayMillis <= 0) {
            throw new IllegalArgumentException(
                    "initialDelayMillis must be greater than zero."
            );
        }

        if (multiplier < 1.0 || !Double.isFinite(multiplier)) {
            throw new IllegalArgumentException(
                    "multiplier must be finite and greater than or equal to 1.0."
            );
        }

        if (clock == null) {
            throw new IllegalArgumentException(
                    "clock must not be null."
            );
        }

        this.maxRetries = maxRetries;
        this.initialDelayMillis = initialDelayMillis;
        this.multiplier = multiplier;
        this.clock = clock;
    }

    @Override
    public Optional<Instant> nextAttemptAt(
            OutboxEvent event,
            DeliveryResult result
    ) {

        if (result.failureType() != FailureType.TRANSIENT) {
            return Optional.empty();
        }

        if (event.getRetryCount() >= maxRetries) {
            return Optional.empty();
        }

        int retryNumber =
                event.getRetryCount() + 1;

        double multiplierPower =
                Math.pow(
                        multiplier,
                        retryNumber - 1
                );

        double delayMillis =
                initialDelayMillis * multiplierPower;

        if (!Double.isFinite(delayMillis)
                || delayMillis > Long.MAX_VALUE) {

            throw new IllegalStateException(
                    "Calculated retry delay is too large."
            );
        }

        long delayMillisLong =
                (long) delayMillis;

        Instant nextAttempt =
                Instant.now(clock)
                        .plusMillis(delayMillisLong);

        return Optional.of(nextAttempt);
    }
}