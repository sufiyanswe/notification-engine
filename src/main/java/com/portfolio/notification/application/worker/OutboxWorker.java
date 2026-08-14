package com.portfolio.notification.application.worker;

import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Component
public class OutboxWorker {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxClaimService outboxClaimService;
    private final OutboxProcessor outboxProcessor;
    private final ApplicationProperties applicationProperties;
    private final Clock clock;

    public OutboxWorker(
            OutboxClaimService outboxClaimService,
            OutboxProcessor outboxProcessor,
            ApplicationProperties applicationProperties,
            Clock clock
    ) {
        this.outboxClaimService = outboxClaimService;
        this.outboxProcessor = outboxProcessor;
        this.applicationProperties = applicationProperties;
        this.clock = clock;
    }

    @Scheduled(
            fixedDelayString = "${application.worker.poll-interval-ms}"
    )
    public void processPendingEvents() {

        Instant now = Instant.now(clock);

        List<UUID> claimedEventIds =
                outboxClaimService.claimPendingEvents(
                        applicationProperties
                                .worker()
                                .batchSize(),
                        now
                );

        if (claimedEventIds.isEmpty()) {
            return;
        }

        log.debug(
                "Processing {} claimed outbox event(s).",
                claimedEventIds.size()
        );

        for (UUID eventId : claimedEventIds) {

            try {

                outboxProcessor.process(eventId);

            } catch (Exception ex) {

                log.error(
                        "Failed to process claimed OutboxEvent [id={}]",
                        eventId,
                        ex
                );
            }
        }
    }
}