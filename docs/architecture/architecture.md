# Architecture

## Overview

Notification Engine follows a layered architecture while selectively applying the Ports and Adapters (Hexagonal Architecture) pattern for integrating external systems.

The architecture separates HTTP concerns, application use cases, business rules, and infrastructure implementations. Business logic remains independent of external technologies, allowing infrastructure components to evolve without impacting the core application.

Beginning with **Sprint 6**, the application adopts the **Transactional Outbox Pattern**, decoupling notification creation from notification delivery while ensuring reliable persistence.

The primary objectives of this architecture are:

- Clear separation of responsibilities
- Low coupling between layers
- High cohesion within components
- Replaceable infrastructure
- Extensible integration points
- Testable business logic
- Reliable persistence
- Incremental system evolution

---

# Architectural Goals

The architecture is designed around the following principles:

- Clear separation of concerns
- Explicit dependency direction
- Replaceable infrastructure
- Extensible integrations
- Maintainable codebase
- Testable business logic
- Consistent package organization

Every new feature introduced into the project should preserve these principles.

---

# High-Level Architecture

```
                    HTTP Request
                          │
                          ▼
                    API Layer
                          │
                          ▼
               Application Layer
                  │              │
                  ▼              ▼
             Domain Layer   Infrastructure Layer
                  │              │
                  └──────┬───────┘
                         ▼
                    PostgreSQL
```

The API layer coordinates client communication.

The Application layer orchestrates business workflows.

The Domain layer represents business concepts and business rules.

The Infrastructure layer provides technology-specific implementations.

---

# Architectural Patterns

The project combines multiple architectural styles where appropriate.

## Layered Architecture

The application is organized into distinct layers:

- API
- Application
- Domain
- Infrastructure

Each layer has a clearly defined responsibility.

---

## Ports and Adapters (Hexagonal Architecture)

External integrations are implemented using the Ports and Adapters pattern.

The application depends on abstractions rather than concrete implementations.

Notification creation no longer performs synchronous delivery. Instead, it records work by persisting an `OutboxEvent`.

```
HTTP Request
      │
      ▼
NotificationService
      │
      ├── Persist Notification
      │
      ├── Persist OutboxEvent
      │
      ▼
COMMIT

────────────────────────

Background Worker (Sprint 7)
      │
      ▼
NotificationChannel
      │
      ▼
NotificationChannelResolver
      │
 ┌──────┼───────────────┐
 ▼      ▼               ▼
Email   SMS           Push
```

The `NotificationChannel` abstraction remains the integration point for external providers, but it is now consumed by the background worker instead of `NotificationService`.

---

## Repository Pattern

Persistence is accessed through repository abstractions.

Application services interact with repositories rather than persistence technologies directly.

---

## Transactional Outbox Pattern

Notification creation persists both the business data (`Notification`) and the integration work (`OutboxEvent`) within a single database transaction.

This guarantees that both records are committed together or rolled back together, eliminating inconsistent states and providing the foundation for reliable asynchronous processing.

---

# Layers

## API Layer

### Responsibility

Expose HTTP endpoints and translate HTTP requests into application use cases.

Contains:

- Controllers
- Request models
- Response models

The API layer contains no business logic.

Current package:

```
api/
```

---

## Application Layer

### Responsibility

Coordinate business workflows.

Application services orchestrate domain objects and infrastructure components while remaining independent of HTTP concerns.

Typical responsibilities include:

- Executing use cases
- Coordinating persistence
- Managing transactional boundaries
- Creating OutboxEvents

Current package:

```
application/
```

---

## Domain Layer

### Responsibility

Represent the core business concepts of the Notification Engine.

The domain contains business rules and business state.

The domain should remain independent from Spring Framework whenever practical.

Contains:

- Domain entities
- Domain value objects
- Domain enums
- Domain ports
- Repository abstractions

Current package:

```
domain/
```

---

## Infrastructure Layer

### Responsibility

Provide technology-specific implementations required by the application.

Examples include:

- Spring Data repositories
- Notification delivery adapters
- Delivery channel resolution
- Security configuration
- External integrations

Current package:

```
infrastructure/
```

---

# Dependency Rules

Dependencies should always point toward business logic.

```
API
    │
    ▼
Application
   ↙     ↘
Domain  Infrastructure
```

Allowed dependencies:

- API → Application
- Application → Domain
- Application → Infrastructure
- Infrastructure → External Systems

The following dependencies are not allowed:

- Domain → Spring MVC
- Domain → Controllers
- Domain → Spring Data
- API → Database
- Controllers → Repositories
- Infrastructure → Controllers

---

# Current Implementation

```
NotificationEngineApplication

API
 └── NotificationController

Application
 └── NotificationService

Domain
 ├── Notification
 ├── OutboxEvent
 ├── NotificationStatus
 ├── OutboxStatus
 ├── NotificationChannelType
 ├── OutboxEventType
 ├── NotificationChannel
 ├── NotificationRepository
 └── OutboxRepository

Infrastructure
 ├── NotificationChannelResolver
 ├── EmailNotificationChannel
 ├── SmsNotificationChannel
 ├── PushNotificationChannel
 ├── NotificationRepository (Spring Data)
 ├── OutboxRepository (Spring Data)
 ├── SecurityConfig
 └── ApplicationProperties
```

---

# Notification Processing Flow

```
HTTP Request
      │
      ▼
NotificationService
      │
      ├── Persist Notification
      │
      ├── Persist OutboxEvent
      │
      ▼
COMMIT
      │
      ▼
HTTP 201 Created

────────────────────────
```

Notification creation is now independent of external notification providers.

When a notification is created, the application persists both the `Notification` and its corresponding `OutboxEvent` within a single database transaction.

Delivery is performed asynchronously by a background worker that processes pending `OutboxEvent` records.

This design implements the **Transactional Outbox Pattern**, ensuring reliable persistence while decoupling HTTP request processing from external delivery providers.

---

# Design Principles

The project currently applies the following engineering principles:

- Layered Architecture
- Ports and Adapters (Hexagonal Architecture)
- Repository Pattern
- Transactional Outbox Pattern
- Single Responsibility Principle
- Open/Closed Principle
- Dependency Inversion Principle
- Constructor-based Dependency Injection
- Thin Controllers
- Rich Domain Model
- Explicit Package Organization

These principles should remain consistent as the project evolves.

---

# Future Evolution

The current architecture is intentionally designed to support future capabilities without significant structural changes.

Planned areas of evolution include:

- Background Worker
- Retry mechanism
- Scheduled delivery
- Dead Letter Queue (DLQ)
- Monitoring and metrics
- Distributed tracing
- Kafka integration
- Event-driven integrations