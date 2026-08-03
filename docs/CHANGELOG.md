# Changelog

All notable changes to this project are documented in this file.

The format is inspired by [Keep a Changelog](https://keepachangelog.com/), and the project follows Semantic Versioning during development.

---

## [0.2.0] - 2026-08-03

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

## [0.1.0] - 2026-07-XX

### Added

- Initial Spring Boot project
- Layered architecture
- Notification domain model
- REST API foundation
- PostgreSQL integration
- Spring Data JPA
- Flyway integration
- Security configuration