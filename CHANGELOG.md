# Changelog

All notable changes to this project will be documented in this file.

The format is based on Keep a Changelog.

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
- Domain behavior for failed delivery (markAsFailed()).
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
- Notification state transition (markAsSent())
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