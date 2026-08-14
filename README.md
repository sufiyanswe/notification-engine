
# Notification Delivery Engine

A production-oriented backend service for reliable notification delivery across multiple delivery channels, built with Spring Boot and PostgreSQL.

The system is designed around a set of failure modes that commonly
affect background delivery systems:

-   A process crashes after persisting a notification but before
    delivering it.
-   Multiple workers attempt to process the same work item concurrently.
-   A transient delivery failure requires bounded retries.
-   A worker crashes while holding an in-progress work item.
-   A delivery operation is replayed after recovery.

The implementation uses a transactional outbox, PostgreSQL row locking,
lease-based recovery, structured failure classification, and
configurable exponential backoff to make these failure modes explicit
and testable.

> **Status:** Active development · **Version:** `0.6.0-SNAPSHOT`

------------------------------------------------------------------------

## Problem Statement

A naive notification service can lose work or produce inconsistent state
when persistence and external delivery are treated as one operation.

For example:

``` text
1. Save notification
2. Application crashes
3. The notification is never delivered
```

The opposite failure is also possible:

``` text
1. Send email
2. Application crashes before recording success
3. Work is retried
4. Email may be sent again
```

This project separates durable work creation from background delivery
and treats retries, concurrency, and worker failure as explicit parts of
the system design.

------------------------------------------------------------------------

## Architecture

```text
                         HTTP Client
                             │
                             │ POST /api/v1/notifications
                             ▼
                    ┌───────────────────┐
                    │    API Layer      │
                    │ Controller / DTO  │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │ Application Layer │
                    │ Notification Flow │
                    └─────────┬─────────┘
                              │
                              ▼
                    ┌───────────────────┐
                    │      Domain       │
                    │ Notification      │
                    │ OutboxEvent       │
                    └─────────┬─────────┘
                              │
                              │ transactional write
                              ▼
                    ┌───────────────────┐
                    │    PostgreSQL     │
                    │ Notifications     │
                    │ Outbox Events     │
                    └─────────┬─────────┘
                              │
                              │ background polling
                              ▼
                    ┌───────────────────┐
                    │  OutboxProcessor  │
                    └─────────┬─────────┘
                              │
                              ▼
                 ┌──────────────────────────┐
                 │ NotificationChannel      │
                 │ Resolver                 │
                 └────────────┬─────────────┘
                              │
                  resolves requested channel
                              │
             ┌────────────────┼────────────────┐
             ▼                ▼                ▼
      ┌─────────────┐  ┌─────────────┐  ┌─────────────┐
      │    Email    │  │     SMS     │  │    Push     │
      │   Channel   │  │   Channel   │  │   Channel   │
      └──────┬──────┘  └──────┬──────┘  └──────┬──────┘
             │                │                │
             └────────────────┼────────────────┘
                              ▼
                       DeliveryResult
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
                 SUCCESS             FAILURE
                                        │
                               ┌────────┴────────┐
                               ▼                 ▼
                           TRANSIENT          PERMANENT
                               │                 │
                             RETRY              FAIL
```

The system uses layered architecture with Ports and Adapters boundaries around
infrastructure-dependent operations.

`NotificationChannelResolver` is the channel-selection boundary. It resolves
the appropriate `NotificationChannel` implementation from the notification's
requested delivery channel (`EMAIL`, `SMS`, or `PUSH`).

This keeps channel selection separate from the delivery workflow. The
`OutboxProcessor` does not need channel-specific branching; it resolves the
channel and processes the resulting `DeliveryResult` through the same retry,
failure, and recovery pipeline.

The domain model owns business state transitions. Application services
coordinate workflows, while infrastructure adapters provide persistence and
delivery implementations.

Detailed design documentation is available in:

- `docs/architecture/architecture.md`
- `docs/adr/`
- `docs/diagrams/request-flow.md`

## Core Engineering Decisions

### Transactional Outbox

Notification creation and its corresponding outbox event are persisted
in the same database transaction.

``` text
Database Transaction
┌──────────────────────────────────┐
│ INSERT notification              │
│ INSERT outbox_event              │
└──────────────────────────────────┘
              │
              ▼
          COMMIT
              │
              ▼
     Background delivery
```

This prevents a successfully committed notification from existing
without a durable work item for delivery.

See: `docs/adr/ADR-004-transactional-outbox.md`

### Concurrent Worker Safety

Multiple worker instances can claim work concurrently using PostgreSQL:

``` sql
FOR UPDATE SKIP LOCKED
```

This allows workers to skip rows currently claimed by another worker
instead of blocking on the same work item.

The locking strategy is part of the correctness model for multi-worker
processing.

See: `docs/adr/ADR-005-background-worker.md`

### Lease-Based Processing

An outbox event receives a lease while it is being processed.

``` text
PENDING
   │
   ▼
PROCESSING + lease_until
   │
   ├── success ─────────────► PROCESSED
   │
   ├── transient failure ───► PENDING
   │
   └── worker crash
             │
             ▼
       lease expires
             │
             ▼
          recovery
             │
             ▼
          PENDING
```

If a worker disappears while processing an event, the expired lease
makes the event eligible for recovery.

See: `docs/adr/ADR-007-lease-based-outbox-processing.md`

### Structured Failure Classification

Delivery failures are represented using structured failure types rather
than parsing exception messages or human-readable error strings.

``` text
DeliveryResult
      │
      ├── success
      │
      └── failure
            │
            ├── TRANSIENT
            │      └── retry
            │
            └── PERMANENT
                   └── fail
```

This keeps retry decisions deterministic and independent of
error-message formatting.

See: `docs/adr/ADR-001-structured-failure-classification.md`\
See: `docs/adr/ADR-002-retry-decision-failure-type.md`

### Pluggable Retry Policy

Retry timing is represented by a `RetryPolicy` abstraction rather than
being embedded inside the outbox entity or processor.

The current implementation uses exponential backoff:

``` text
Retry 1 → 1s
Retry 2 → 2s
Retry 3 → 4s
Retry 4 → 8s
Retry 5 → 16s
```

After the configured retry limit is exhausted, the event transitions to
`FAILED`.

See: `docs/adr/ADR-006-retry-and-exponential-backoff.md`

------------------------------------------------------------------------

## Outbox Lifecycle

The outbox event has its own lifecycle independent of the notification
lifecycle.

``` text
             ┌──────────────┐
             │    PENDING   │
             └──────┬───────┘
                    │ claim
                    ▼
             ┌──────────────┐
             │  PROCESSING  │
             └──────┬───────┘
                    │
          ┌─────────┼──────────┐
          │         │          │
       success   transient   permanent /
          │       failure     exhausted
          ▼         │          │
     PROCESSED      ▼          ▼
                 PENDING     FAILED
                   │
                   └── retry
```

The `lease_until` timestamp provides recovery semantics for events left
in `PROCESSING` after a worker interruption.

------------------------------------------------------------------------

## Notification Lifecycle

The notification aggregate currently models:

``` text
PENDING
   │
   ├──────────────► SENT
   │
   └──────────────► FAILED
```

The domain entity owns valid lifecycle transitions rather than allowing
arbitrary status mutation from application code.

------------------------------------------------------------------------

## API

### Create Notification

``` http
POST /api/v1/notifications
Content-Type: application/json
```

Example request:

``` json
{
  "recipientId": "user@example.com",
  "title": "Example notification",
  "message": "Your notification has been queued.",
  "deliveryChannel": "EMAIL"
}
```

Example:

``` bash
curl -X POST http://localhost:8080/api/v1/notifications \
  -H "Content-Type: application/json" \
  -d '{
    "recipientId": "user@example.com",
    "title": "Example notification",
    "message": "Your notification has been queued.",
    "deliveryChannel": "EMAIL"
  }'
```

The endpoint creates the notification and its durable outbox work item
within the same transaction. The requested `deliveryChannel` determines
which channel implementation the background worker will resolve.

### Request Validation

The API validates:

-   Required recipient
-   Required message
-   Delivery channel enum
-   Request payload constraints
-   Domain state transitions

------------------------------------------------------------------------

## Delivery

The delivery layer is built around the `NotificationChannel` abstraction.

`NotificationChannelResolver` selects the correct channel implementation based
on the notification's `deliveryChannel`.

```text
Notification
     │
     │ deliveryChannel
     ▼
NotificationChannelResolver
     │
     ├── EMAIL ──► EmailNotificationChannel
     │
     ├── SMS ────► SmsNotificationChannel
     │
     └── PUSH ───► PushNotificationChannel
                         │
                         ▼
                   DeliveryResult
```

The important separation is:

```text
OutboxProcessor
      │
      ▼
NotificationChannelResolver
      │
      ▼
NotificationChannel
      │
      ▼
DeliveryResult
      │
      ├── SUCCESS
      │
      └── FAILURE
            ├── TRANSIENT  → RetryPolicy
            └── PERMANENT  → Mark failed
```

The current codebase includes Email, SMS, and Push channel implementations.
Provider-specific integrations remain infrastructure concerns. A channel
implementation does not by itself imply that a live third-party provider is
configured or that production delivery has been verified.

## Retry Model

Transient failures are retried using the configured `RetryPolicy`.

Current configuration:

Property                             Value
  -------------------- ---------------------
Maximum retries                          5
Initial delay                     1 second
Backoff multiplier                     2.0
Retry delays           1s, 2s, 4s, 8s, 16s

Permanent failures are not retried.

Once the retry limit is exhausted, the outbox event is marked `FAILED`
and the failure reason is persisted.

------------------------------------------------------------------------

## Persistence

The service uses PostgreSQL as its durable state store.

### Main persistence models

``` text
notifications
     │
     ├── notification identity
     ├── recipient
     ├── message
     ├── delivery channel
     ├── status
     └── failure reason

outbox_events
     │
     ├── event identity
     ├── processing status
     ├── retry count
     ├── next attempt time
     ├── lease_until
     ├── failure reason
     └── processed timestamp
```

Database schema changes are managed through Flyway migrations.

------------------------------------------------------------------------

## Technology Stack

### Backend

-   Java 25
-   Spring Boot 3.5.16
-   Spring MVC
-   Spring Data JPA
-   Hibernate
-   Spring Validation

### Persistence

-   PostgreSQL
-   Flyway

### Testing

-   JUnit
-   Spring Boot Test
-   Testcontainers
-   PostgreSQL Testcontainers

### Build

-   Maven Wrapper

------------------------------------------------------------------------

## Project Structure

``` text
src/main/java/com/portfolio/notification

├── api
├── application
├── domain
└── infrastructure
```

The major boundaries are:

-   `api` --- HTTP contracts and request handling
-   `application` --- application workflows and orchestration
-   `domain` --- business rules, entities, value objects, and domain
    policies
-   `infrastructure` --- persistence, database, delivery adapters, and
    framework integration

------------------------------------------------------------------------

## Testing

The project includes unit and integration tests covering the reliability
mechanisms implemented by the service.

The integration test suite uses PostgreSQL through Testcontainers for
database behavior that cannot be reliably represented by mocks.

Current verified areas include:

-   Notification lifecycle rules
-   Delivery result classification
-   Retry policy behavior
-   Exponential backoff
-   Retry exhaustion
-   Outbox processing
-   Outbox persistence
-   Concurrent outbox claiming
-   PostgreSQL `SKIP LOCKED` behavior
-   Lease/recovery behavior
-   Flyway migration compatibility

Run the test suite with:

``` bash
./mvnw test
```

On Windows PowerShell:

``` powershell
.\mvnw test
```

------------------------------------------------------------------------

## Running Locally

### Requirements

-   Java 25
-   PostgreSQL
-   Git

### Start the application

``` bash
./mvnw spring-boot:run
```

Windows PowerShell:

``` powershell
.\mvnw spring-boot:run
```

Flyway migrations are applied and validated during application startup.

------------------------------------------------------------------------

## Reliability Model

The system is intentionally designed around **at-least-once background
processing**.

That distinction is important.

A worker can successfully claim an event and still crash before
completing the entire processing workflow. Lease recovery therefore
permits the same logical work item to be processed again.

This creates two separate idempotency concerns:

``` text
1. Domain-state idempotency
   └── Is the notification already SENT?

2. External delivery idempotency
   └── Has the external provider already accepted the delivery?
```

The first concern is part of the domain/application design.

The second requires an idempotency mechanism at the external delivery
boundary or provider-supported idempotency semantics.

The current implementation does not claim exactly-once external
delivery.

------------------------------------------------------------------------

## Known Limitations

The following capabilities are not yet represented as completed
production features:

-   Client-facing `Idempotency-Key` enforcement
-   Replay-safe external delivery
-   Live external provider integrations (for example AWS SES, SMS provider, and push provider)
-   WireMock-based external provider resilience tests
-   Micrometer/Prometheus operational metrics
-   Structured JSON logging and MDC correlation
-   k6 performance benchmarks
-   Production Docker/Compose configuration
-   CI/CD pipeline
-   AWS deployment
-   Production alerting configuration

These are infrastructure and operational capabilities that build on the
current reliability foundation.

------------------------------------------------------------------------

## Documentation

  -------------------------------------------------------------------------
Document                              Purpose
  ------------------------------------- -----------------------------------
`docs/architecture/architecture.md`   System architecture and component
boundaries

`docs/adr/`                           Architectural decisions and
tradeoffs

`docs/diagrams/request-flow.md`       Request and processing flow

`docs/engineering/`                   Engineering standards and
implementation guidance
  -------------------------------------------------------------------------

------------------------------------------------------------------------

## Engineering Principles

The implementation prioritizes:

-   Explicit domain state transitions
-   Transactional consistency
-   At-least-once processing semantics
-   Safe concurrent work claiming
-   Deterministic retry decisions
-   Bounded retries
-   Failure recovery
-   Testable infrastructure boundaries
-   Database-backed durability
-   Clear separation between business rules and infrastructure concerns

The goal is not to hide distributed-systems failure modes behind
framework abstractions, but to model them explicitly and make their
behavior testable.

------------------------------------------------------------------------

## License

This project is intended as a public engineering portfolio project.
