# Notification Engine

A production-oriented backend service built with Spring Boot that manages and delivers notifications through multiple delivery channels.

The project is designed as an engineering portfolio that demonstrates backend architecture, clean code, and scalable system design rather than simply showcasing framework features.

---

# Project Goals

The Notification Engine explores how production backend systems evolve over time while maintaining a clean and extensible architecture.

The project emphasizes:

* Layered Architecture
* Ports and Adapters (Hexagonal Architecture)
* Domain-Driven Design (DDD) principles
* Extensible notification delivery
* Production-ready engineering practices
* Maintainable and testable business logic

---

# Current Features

## Notification Management

* Create notifications
* Persist notifications
* Track notification lifecycle
* UUID-based identifiers

## Notification Delivery

* Email notifications
* SMS notifications
* Push notifications

Delivery channels are resolved dynamically through the `NotificationChannel` abstraction, allowing new delivery mechanisms to be added without modifying existing application services.

Notifications transition through the following lifecycle:

```text
                +---------+
                | PENDING |
                +---------+
                     |
         +-----------+-----------+
         |                       |
         v                       v
     +--------+             +---------+
     |  SENT  |             | FAILED  |
     +--------+             +---------+
                                  |
                                  v
                    failureReason persisted
```

The `Notification` aggregate owns all valid state transitions, while the Application layer coordinates delivery using `DeliveryResult`.

## Validation

* Bean Validation
* Request validation
* Enum validation
* Domain state validation

## Persistence

* PostgreSQL
* Spring Data JPA
* Hibernate
* Flyway migrations
* Hibernate Dirty Checking

---

# Technology Stack

## Backend

* Java 25
* Spring Boot
* Spring MVC
* Spring Data JPA
* Hibernate
* Flyway

## Database

* PostgreSQL

## Build

* Maven

---

# Architecture

The project follows a layered architecture while selectively applying the Ports and Adapters (Hexagonal Architecture) pattern for notification delivery.

```text
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

The architecture separates HTTP concerns, business workflows, domain rules, and infrastructure implementations to keep business logic independent from external technologies.

Detailed documentation:

* **Architecture:** `docs/architecture/architecture.md`
* **Architecture Decision Records (ADRs):** `docs/adr/`
* **Request Flow:** `docs/diagrams/request-flow.md`

---

# Project Structure

```text
src/main/java/com/portfolio/notification

├── api
├── application
├── domain
└── infrastructure
```

---

# Running the Project

## Requirements

* Java 25
* PostgreSQL
* Maven

## Start

```bash
./mvnw spring-boot:run
```

---

# Database

Flyway automatically manages database schema migrations during application startup.

---

# Current Status

**Current Version:** `v0.2.0`

Implemented:

* Layered Architecture
* Ports and Adapters
* Notification Domain Model
* Notification Delivery Pipeline
* Email Adapter
* SMS Adapter
* Push Adapter
* Notification Channel Resolver
* Transaction Management
* Hibernate Dirty Checking
* Notification Lifecycle Management

---

# Roadmap

Planned features include:

* Retry mechanism
* Scheduled delivery
* Authentication & Authorization
* Asynchronous processing
* Outbox Pattern
* Event-driven notifications
* Monitoring & Metrics
* Distributed tracing
* Docker
* CI/CD Pipeline

---

# Documentation

| Document     | Description                                       |
| ------------ | ------------------------------------------------- |
| Architecture | Overall system architecture and design principles |
| ADR          | Architectural Decision Records                    |
| Request Flow | End-to-end request lifecycle                      |

---

# License

This project is intended for educational and portfolio purposes.
