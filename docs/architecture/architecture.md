# Architecture

## Overview

Notification Engine uses a layered architecture with Ports and Adapters
principles at integration boundaries.

```text
API
 │
 ▼
Application
 │
 ▼
Domain
 ▲
 │
Infrastructure
 │
 ├── PostgreSQL
 └── External Delivery Systems
```

The architecture separates HTTP concerns, application workflows, domain
rules, persistence, and external integrations.

## Core Patterns

### Layered Architecture

- **API** — HTTP controllers and request/response models.
- **Application** — use-case orchestration and background processing.
- **Domain** — business state, rules, entities, ports, and repository
  abstractions.
- **Infrastructure** — PostgreSQL persistence, delivery adapters,
  configuration, and framework-specific implementations.

### Transactional Outbox

Notification creation persists both the `Notification` and its
`OutboxEvent` in the same database transaction.

```text
HTTP Request
     │
     ▼
NotificationService
     │
     ├── Notification
     └── OutboxEvent
            │
          COMMIT
            │
            ▼
     Background Processing
```

This decouples HTTP request processing from external notification
delivery while keeping delivery work durable.

### Asynchronous Processing

Eligible `PENDING` outbox events are claimed by the background worker.

```text
OutboxWorker
     │
     ▼
OutboxClaimService
     │
     ▼
PROCESSING + lease
     │
     ▼
OutboxProcessor
     │
     ▼
NotificationChannel
     │
 ┌───┼────┐
 ▼   ▼    ▼
Email SMS Push
```

Claims use database locking with `FOR UPDATE SKIP LOCKED` so multiple
workers can process events concurrently without claiming the same row.

## Retry and Recovery

### Retry

Transient delivery failures are handled by `RetryPolicy`.

- Exponential backoff
- Configurable maximum retries
- Persistent `retryCount`
- Persistent `nextAttemptAt`
- Persistent `lastFailureReason`

Retryable failures return the event to `PENDING`. Terminal failures
transition the notification and outbox event to `FAILED`.

### Lease Recovery

A claimed event receives a `leaseUntil` timestamp.

If processing is abandoned and the lease expires:

```text
PROCESSING
    │
    │ lease expired
    ▼
  PENDING
```

`OutboxRecoveryService` safely recovers expired events using database
locking. The processing lease is cleared and the event becomes eligible
for processing again.

## Delivery Semantics

The system provides **at-least-once processing**.

Exactly-once external delivery is not guaranteed. A worker can succeed
with an external provider and crash before recording the result in the
database, allowing the event to be processed again after lease recovery.

## Key Rules

- Keep business rules in the domain layer.
- Keep persistence and external-system details in infrastructure.
- Use repository abstractions instead of direct persistence access.
- Keep retry decisions inside `RetryPolicy`.
- Keep lease recovery separate from normal delivery processing.
- Preserve explicit `OutboxEvent` state transitions.
