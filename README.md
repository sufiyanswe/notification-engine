# Notification Delivery Engine

A Java and Spring Boot backend for reliable asynchronous notification processing, built with PostgreSQL.

The project explores how to design and test notification workflows that remain recoverable when workers fail, delivery attempts are retried, or multiple workers process background work concurrently.

> **Status:** Active development — `0.6.0-SNAPSHOT`
>
> The reliability foundation is implemented and tested. Request-level idempotency, replay-safe external delivery, live provider integrations, observability, performance testing, CI/CD, and production deployment remain in progress.
>
> This project does **not** claim exactly-once external delivery or production readiness.

## Engineering Focus

- Transactional Outbox Pattern
- PostgreSQL concurrent work claiming with `FOR UPDATE SKIP LOCKED`
- Lease-based processing and recovery
- Structured transient/permanent failure classification
- Configurable retry policies with exponential backoff
- Spring Data JPA/Hibernate persistence
- Flyway database migrations
- Unit and PostgreSQL integration testing with Testcontainers

## Problem Statement

A naive notification service can lose work or produce inconsistent state when persistence and external delivery are treated as one operation.

For example:

```text
1. Save notification
2. Application crashes
3. Notification is never delivered
```

The opposite failure is also possible:

```text
1. Send notification
2. Application crashes before recording success
3. Work is retried
4. Notification may be sent again
```

This project separates durable work creation from background processing and treats retries, concurrency, and worker failure as explicit parts of the system design.

## Architecture

```text
HTTP Client
    |
    v
API Layer: Controller / DTO / Validation
    |
    v
Application Layer: Notification Workflow
    |
    v
Domain: Notification + OutboxEvent
    |
    | transactional write
    v
PostgreSQL
    |
    | background polling and work claiming
    v
OutboxProcessor
    |
    v
NotificationChannelResolver
    |
    v
NotificationChannel
    |
    v
DeliveryResult
    |
    +--> SUCCESS     --> complete processing
    +--> TRANSIENT   --> retry according to policy
    +--> PERMANENT   --> mark processing as failed
```

The codebase uses layered boundaries with Ports and Adapters concepts around infrastructure-dependent operations. Application services coordinate workflows, domain objects own business state transitions, and infrastructure adapters provide persistence and delivery implementations.

Detailed design material is available in:

- `docs/architecture/architecture.md`
- `docs/adr/`
- `docs/diagrams/request-flow.md`
- `docs/engineering/`

## Core Engineering Decisions

### Transactional Outbox

Notification creation and its corresponding outbox event are persisted in the same database transaction. This prevents a committed notification from existing without a durable work item for background processing.

### Concurrent Worker Safety

Multiple workers can claim background work concurrently using PostgreSQL row locking with:

```sql
FOR UPDATE SKIP LOCKED
```

Workers can skip rows currently claimed by another worker instead of blocking on the same work item.

### Lease-Based Recovery

An outbox event receives a lease while it is being processed. If a worker disappears before completing the workflow, the expired lease makes the event eligible for recovery.

```text
PENDING -> PROCESSING + lease_until -> PROCESSED
                    |
                    +-> transient failure -> PENDING
                    |
                    +-> worker interruption -> lease expiry -> recovery
```

### Structured Failure Classification

Delivery failures are represented using structured failure types rather than parsing human-readable exception messages. Retry decisions therefore remain deterministic and independent of error-message formatting.

### Retry Policy

Retry timing is represented through a `RetryPolicy` abstraction. The current implementation uses exponential backoff and bounded retries. After the configured retry limit is exhausted, the outbox event transitions to `FAILED`.

## API

### Create Notification

```http
POST /api/v1/notifications
Content-Type: application/json
```

Example request:

```json
{
  "recipientId": "user@example.com",
  "title": "Example notification",
  "message": "Your notification has been queued.",
  "deliveryChannel": "EMAIL"
}
```

The endpoint validates the request and creates the notification together with its durable outbox work item in one transaction.

## Delivery Model

The delivery layer is built around the `NotificationChannel` abstraction. `NotificationChannelResolver` selects the channel implementation based on the requested delivery channel.

The current codebase includes Email, SMS, and Push channel implementations. A channel implementation does not by itself imply that a live third-party provider is configured or that production delivery has been verified.

The external delivery boundary is intentionally treated as an at-least-once processing concern. The project does not claim exactly-once external delivery.

## Persistence

PostgreSQL is used as the durable state store.

The main persistence concepts are:

- `notifications` — notification identity, recipient, message, channel, status, and failure information
- `outbox_events` — event identity, processing status, retry count, next-attempt time, lease information, failure information, and processed timestamp

Database schema changes are managed through Flyway migrations.

## Technology Stack

### Backend

- Java 25
- Spring Boot 3.5.16
- Spring MVC
- Spring Data JPA
- Hibernate
- Spring Validation

### Persistence

- PostgreSQL
- Flyway

### Testing

- JUnit
- Spring Boot Test
- Mockito
- Testcontainers
- PostgreSQL Testcontainers

### Build and Tools

- Maven Wrapper
- Git
- Docker

## Project Structure

```text
src/main/java/com/portfolio/notification
├── api
├── application
├── domain
└── infrastructure
```

- `api` — HTTP contracts and request handling
- `application` — workflows and orchestration
- `domain` — business rules, entities, value objects, and policies
- `infrastructure` — persistence, database, delivery adapters, and framework integration

## Testing

The project includes unit and integration tests for the reliability mechanisms implemented by the service. PostgreSQL integration tests use Testcontainers for database behavior that cannot be reliably represented by mocks.

Tested areas include:

- Notification lifecycle rules
- Delivery result classification
- Retry policy behavior and exponential backoff
- Retry exhaustion
- Outbox persistence and processing
- Concurrent outbox claiming
- PostgreSQL `SKIP LOCKED` behavior
- Lease and recovery behavior
- Flyway migration compatibility

Run the test suite:

```bash
./mvnw test
```

Windows PowerShell:

```powershell
.\mvnw test
```

## Running Locally

### Requirements

- Java 25
- PostgreSQL
- Git

Start the application:

```bash
./mvnw spring-boot:run
```

Windows PowerShell:

```powershell
.\mvnw spring-boot:run
```

Flyway migrations are applied and validated during application startup.

## Known Limitations

The following capabilities are not yet represented as completed production features:

- Client-facing `Idempotency-Key` enforcement
- Replay-safe external delivery
- Live external provider integrations
- WireMock-based provider resilience tests
- Micrometer/Prometheus operational metrics
- Structured JSON logging and MDC correlation
- k6 performance benchmarks
- Production Docker/Compose configuration
- CI/CD pipeline
- AWS deployment
- Production alerting configuration

These capabilities build on the current reliability foundation.

## Engineering Principles

The implementation prioritizes:

- Explicit domain state transitions
- Transactional consistency
- At-least-once processing semantics
- Safe concurrent work claiming
- Deterministic retry decisions
- Bounded retries
- Failure recovery
- Testable infrastructure boundaries
- Database-backed durability
- Separation between business rules and infrastructure concerns

The goal is to model failure modes explicitly and make their behavior testable rather than hiding them behind framework abstractions.
