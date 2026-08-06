package com.portfolio.notification.application.worker;

import com.portfolio.notification.domain.model.OutboxEvent;
import com.portfolio.notification.domain.repository.OutboxRepository;
import com.portfolio.notification.infrastructure.configuration.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OutboxWorker {

    private static final Logger log =
            LoggerFactory.getLogger(OutboxWorker.class);

    private final OutboxRepository outboxRepository;
    private final OutboxProcessor outboxProcessor;
    private final ApplicationProperties applicationProperties;

    public OutboxWorker(
            OutboxRepository outboxRepository,
            OutboxProcessor outboxProcessor,
            ApplicationProperties applicationProperties
    ) {
        this.outboxRepository = outboxRepository;
        this.outboxProcessor = outboxProcessor;
        this.applicationProperties = applicationProperties;
    }

    @Scheduled(
            fixedDelayString = "${application.worker.poll-interval-ms}"
    )
    public void processPendingEvents() {


        List<OutboxEvent> pendingEvents =
                outboxRepository.findPending(
                        applicationProperties
                                .worker()
                                .batchSize()
                );

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug(
                "Processing {} pending outbox event(s).",
                pendingEvents.size()
        );

        for (OutboxEvent event : pendingEvents) {

            try {

                outboxProcessor.process(event.getId());

            } catch (Exception ex) {

                log.error(
                        "Failed to process OutboxEvent [id={}, notificationId={}, eventType={}]",
                        event.getId(),
                        event.getNotificationId(),
                        event.getEventType(),
                        ex
                );
            }
        }
    }
}