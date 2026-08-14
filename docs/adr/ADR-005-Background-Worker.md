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

## Subsequent Decisions

The background worker introduced by this ADR was extended in subsequent
iterations.

### Retry Scheduling

Retry scheduling with configurable exponential backoff was introduced
to handle transient delivery failures.

See [ADR-006: Introduce Retry Scheduling with Exponential Backoff](ADR-006-Retry-and-Exponential-Backoff.md).

### Lease-Based Processing and Recovery

Lease-based processing was introduced to recover events that remain in
`PROCESSING` after a worker failure or application restart.

See [ADR-007: Introduce Lease-Based Outbox Processing and Recovery](ADR-007-Lease-Based-Outbox-Processing-and-Recovery.md).

## Still Deferred

The following capabilities remain intentionally outside the current
scope:

- Dead-letter queue
- Distributed worker coordination
- Metrics and monitoring