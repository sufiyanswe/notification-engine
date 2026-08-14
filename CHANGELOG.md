# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog.

---

## [0.6.0-SNAPSHOT] - 2026-08-14

### Added

- Structured `FailureType` classification for delivery failures.
- Pluggable `RetryPolicy` abstraction for retry decisions.
- Exponential backoff retry strategy with configurable:
  - Maximum retries
  - Initial retry delay
  - Backoff multiplier
- Retry-aware outbox processing.
- Retry count and next-attempt scheduling for transient failures.
- Lease-based outbox processing using `lease_until`.
- `OutboxClaimService` for concurrency-safe event claiming.
- `OutboxRecoveryService` for recovering events with expired processing leases.
- Scheduled `OutboxRecoveryWorker` for automatic lease recovery.
- PostgreSQL `FOR UPDATE SKIP LOCKED` based outbox claiming.
- Flyway migrations for retry and lease support.
- Testcontainers-based PostgreSQL integration testing.
- Architecture Decision Records for:
  - Retry and exponential backoff
  - Lease-based outbox processing and recovery

### Changed

- Outbox processing now distinguishes between transient and permanent delivery failures.
- Retry decisions are based on structured `FailureType` rather than exception types or human-readable error messages.
- `OutboxProcessor` now delegates retry timing to the `RetryPolicy`.
- Outbox events now maintain processing leases to support worker-crash recovery.
- Outbox lifecycle now supports lease-aware processing:

  - `PENDING`
  - `PROCESSING`
  - `PROCESSED`
  - `FAILED`

- Transient failures return events to `PENDING` with an incremented retry count and scheduled next attempt.
- Permanent failures and exhausted retries transition events to `FAILED`.
- Background workers now use claim-and-lease semantics to support concurrent processing and recovery.

### Database

- Added retry-related outbox fields.
- Added `lease_until` to `outbox_events`.
- Updated outbox indexes to support retry scheduling and lease-based processing.
- Added Flyway migrations V4, V5 and V6.

### Verified

- Full automated test suite: **40 tests, 0 failures, 0 errors, 0 skipped**.
- Clean Maven package build.
- PostgreSQL persistence.
- Concurrent outbox claiming using Testcontainers PostgreSQL.
- Successful notification processing through the background worker.
- Transient failure handling.
- Exponential backoff retry scheduling.
- Retry exhaustion after the configured maximum retry count.
- Permanent failure handling.
- Lease creation during outbox claiming.
- Expired lease detection and recovery.
- Reclaiming of recovered outbox events.
- Manual end-to-end API → Notification → Outbox → Worker → Delivery flow.
- Manual transient failure → retry → exhaustion → `FAILED` flow.

### Known Issue

- Lease recovery can replay an outbox event after the associated notification has already reached `SENT`.
- The current `OutboxProcessor` attempts to mark an already-`SENT` notification as `SENT` again, resulting in an `IllegalStateException`.
- This exposes an idempotency/replay-handling issue that will be addressed in a subsequent change.

---

## [0.5.0] - 2026-08-06

### Added

- Implemented a background worker for asynchronous notification processing.
- Added scheduled polling using Spring's `@Scheduled`.
- Introduced `OutboxProcessor` as the transactional core responsible for processing individual outbox events.
- Added repository support for retrieving pending outbox events in FIFO order.
- Added configurable worker polling interval and batch size through application configuration.
- Added transactional processing with one transaction per outbox event.
- Added failure isolation so one failed event does not interrupt processing of the remaining batch.

### Changed

- Notification delivery is now fully asynchronous.
- Delivery responsibility moved from the request lifecycle to the background worker.
- Introduced explicit outbox lifecycle states:
  - `PENDING`
  - `PROCESSING`
  - `PROCESSED`
  - `FAILED`

### Verified

- Verified scheduler startup and periodic execution.
- Verified successful end-to-end notification processing.
- Verified business failure handling (`DeliveryResult.failure`).
- Verified transaction rollback on unexpected runtime exceptions.
- Verified worker resilience by ensuring one failed event does not stop processing of subsequent events.

## [0.4.0] - 2026-08-06

### Added

- Transactional Outbox Pattern
- OutboxEvent domain aggregate
- OutboxRepository abstraction
- Spring Data Outbox repository implementation
- OutboxEvent database table

### Changed

- Refactored NotificationService to persist Notification and OutboxEvent in a single transaction.
- Notification creation no longer performs synchronous notification delivery.
- Notifications remain in the PENDING state after creation.

### Removed

- Synchronous notification delivery from NotificationService.
- NotificationChannelResolver dependency from NotificationService.
- DeliveryResult handling from NotificationService.

### Database

- Added outbox_events table.
- Added foreign key to notifications.
- Added composite unique constraint.
- Added composite index for pending-event lookup.

### Verification

- Verified atomic transaction rollback.
- Verified Notification persistence.
- Verified OutboxEvent persistence.
- Verified API responses.

---

## [0.3.0] - 2026-08-04

### Added

- FAILED notification lifecycle state.
- Failure reason persisted for unsuccessful deliveries.
- Domain behavior for failed delivery (`markAsFailed()`).
- Flyway migration for failure_reason.
- Delivery simulation for Email, SMS and Push adapters.
- End-to-end success and failure verification.

### Changed

- NotificationService now handles successful and failed delivery outcomes.
- DeliveryResult semantics standardized:
  - success -> reason = null
  - failure -> reason contains business explanation

### Verified

- Email success
- Email failure
- SMS success
- SMS failure
- Push success
- Push failure
- Resolver behavior
- Database persistence
- API responses

---

## [0.2.0] - 2026-08-02

### Added

- Notification delivery pipeline
- NotificationChannel abstraction
- NotificationChannelResolver
- Email notification adapter
- SMS notification adapter
- Push notification adapter
- DeliveryResult domain model
- Notification state transition (`markAsSent()`)
- Delivery channel persistence
- Request validation for delivery channel

### Changed

- NotificationService now orchestrates notification delivery
- Notification entity supports controlled state transitions
- Architecture documentation updated
- Request flow documentation updated

### Verified

- Project build
- Spring Boot application startup
- Flyway database migration
- PostgreSQL persistence
- Email notification delivery
- SMS notification delivery
- Push notification delivery
- Hibernate Dirty Checking
- End-to-end API workflow

---

## [0.1.0] - 2026-07-28

### Added

- Initial Spring Boot project
- Layered architecture
- Notification domain model
- REST API foundation
- PostgreSQL integration
- Spring Data JPA
- Flyway integration
- Security configuration