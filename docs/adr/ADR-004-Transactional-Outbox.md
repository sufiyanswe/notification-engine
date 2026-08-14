# ADR-004 — Adopt Transactional Outbox Pattern

- **Status:** Accepted

## Context

The application originally performed notification delivery synchronously inside NotificationService.

If the database transaction succeeded but delivery failed—or vice versa—the system could become inconsistent.

A more reliable mechanism was required to ensure that accepted notifications are eventually delivered.

## Decision

Adopt the Transactional Outbox Pattern.

NotificationService now performs two operations within the same transaction:

1. Persist Notification
2. Persist OutboxEvent

Background workers will later process pending OutboxEvents asynchronously.

## Consequences

### Positive

- Atomic persistence
- No lost notifications
- Foundation for asynchronous processing
- Easier retry implementation
- Improved reliability

### Negative

- Requires background worker
- Additional database table
- Increased implementation complexity

## Alternatives Considered

### Synchronous Delivery

Rejected due to reliability concerns.

### Message Broker Transactions

Rejected because the project intentionally introduces reliability incrementally before adding external infrastructure.