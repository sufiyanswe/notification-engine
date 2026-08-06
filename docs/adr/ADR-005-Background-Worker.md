# ADR-005: Introduce a Background Worker for Asynchronous Notification Delivery

- **Status:** Accepted

## Context

Prior to Sprint 7, notifications and outbox events were created within a single transaction, but no component existed to process pending outbox events after the transaction committed.

This meant the Outbox Pattern was only partially implemented. The application could reliably record work but could not execute it asynchronously.

The system required a background component capable of processing pending outbox events independently of incoming HTTP requests.

## Decision

Introduce a dedicated background worker inside the existing Spring Boot application.

The worker is responsible for:

- Polling the outbox table at a configurable interval.
- Retrieving a configurable batch of pending events.
- Delegating processing of each event to `OutboxProcessor`.
- Isolating failures so that one failing event does not stop processing of the remaining batch.

Scheduling is implemented using Spring's `@Scheduled` support.

Each `OutboxEvent` is processed within its own transaction.

## Consequences

### Positive

- Notification delivery is fully asynchronous.
- HTTP request latency is no longer affected by notification delivery.
- The worker has a single responsibility.
- Failures are isolated to individual events.
- The architecture is ready for future retry and recovery mechanisms.

### Trade-offs

- Polling introduces a small delivery delay.
- The current implementation supports a single worker instance.
- Infrastructure recovery for abandoned `PROCESSING` events is intentionally deferred.

## Deferred Decisions

The following capabilities are intentionally excluded from Sprint 7 and are planned for future iterations:

- Retry scheduling
- Lock expiration / lease mechanism
- Dead-letter queue
- Distributed workers
- Row-level locking
- Metrics and monitoring

## Alternatives Considered

### Infinite Loop

Rejected because it wastes CPU resources, complicates lifecycle management, and is not idiomatic Spring.

### Message Broker (Kafka / RabbitMQ)

Rejected because introducing external messaging infrastructure was outside the scope of Sprint 7.

### Spring `@Scheduled`

Accepted because it provides a simple, production-proven scheduling mechanism while keeping the architecture easy to understand and extend.