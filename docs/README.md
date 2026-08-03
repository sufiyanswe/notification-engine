# Notification Engine

A production-oriented backend service built with Spring Boot that manages and delivers notifications through multiple delivery channels.

The project is designed as an engineering portfolio focused on backend architecture, clean code, and scalable system design rather than simply demonstrating framework features.

---

## Project Goals

The Notification Engine is built to explore how production backend systems evolve over time.

The project emphasizes:

- Clean Architecture
- Layered Design
- Ports and Adapters (Hexagonal Architecture)
- Domain-Driven Design principles
- Extensible notification delivery
- Production-ready engineering practices

---

## Current Features

### Notification Management

- Create notifications
- Persist notifications
- Track notification status
- UUID-based identifiers

### Notification Delivery

- Email notifications
- SMS notifications
- Push notifications

Delivery channels are resolved dynamically through the `NotificationChannel` abstraction without modifying application services.

### Validation

- Bean Validation
- Request validation
- Enum validation
- Domain state validation

### Persistence

- PostgreSQL
- Spring Data JPA
- Hibernate
- Flyway migrations
- Hibernate Dirty Checking

---

## Technology Stack

### Backend

- Java 25
- Spring Boot
- Spring MVC
- Spring Data JPA
- Hibernate
- Flyway

### Database

- PostgreSQL

### Build

- Maven

---

## Architecture

The project follows a layered architecture while applying the Ports and Adapters (Hexagonal Architecture) pattern for notification delivery.

```
Client
    │
    ▼
Controller
    │
    ▼
Application Service
   ↙        ↘
Domain   Infrastructure
```

Detailed documentation:

- Architecture → `docs/architecture/architecture.md`
- ADRs → `docs/adr/`
- Request Flow → `docs/diagrams/request-flow.md`

---

## Project Structure

```
src/main/java/com/portfolio/notification

├── api
├── application
├── domain
└── infrastructure
```

---

## Running the Project

### Requirements

- Java 25
- PostgreSQL
- Maven

### Start

```bash
./mvnw spring-boot:run
```

---

## Database

Flyway manages the database schema automatically during application startup.

---

## Current Status

Current Version:

**v0.2.0**

Implemented:

- Layered Architecture
- Notification Domain
- Notification Delivery Pipeline
- Email Adapter
- SMS Adapter
- Push Adapter
- Resolver Pattern
- Transaction Management
- Hibernate Dirty Checking

---

## Roadmap

Planned features include:

- Retry mechanism
- Scheduled delivery
- Authentication & Authorization
- Asynchronous processing
- Event-driven notifications
- Outbox Pattern
- Monitoring & Metrics
- Docker
- CI/CD Pipeline

---

## Documentation

| Document | Description |
|----------|-------------|
| Architecture | Overall system design |
| ADR | Architectural decisions |
| Request Flow | Runtime request lifecycle |

---

## License

This project is intended for educational and portfolio purposes.