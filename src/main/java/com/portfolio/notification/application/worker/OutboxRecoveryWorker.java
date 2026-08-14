package com.portfolio.notification.application.worker;

import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;

@Component
public class OutboxRecoveryWorker {

    private final OutboxRecoveryService outboxRecoveryService;
    private final ApplicationProperties applicationProperties;
    private final Clock clock;

    public OutboxRecoveryWorker(
            OutboxRecoveryService outboxRecoveryService,
            ApplicationProperties applicationProperties,
            Clock clock
    ) {
        this.outboxRecoveryService = outboxRecoveryService;
        this.applicationProperties = applicationProperties;
        this.clock = clock;
    }

    @Scheduled(
            fixedDelayString = "${application.worker.recovery-interval-ms}"
    )
    public void recoverExpiredEvents() {

        Instant now = Instant.now(clock);

        outboxRecoveryService.recoverExpiredEvents(
                applicationProperties
                        .worker()
                        .batchSize(),
                now
        );
    }
}