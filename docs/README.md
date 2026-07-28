# Notification Engine

Backend service for receiving, processing, and delivering notifications across multiple communication channels.

> **Project Status:** 🚧 Under Active Development

Notification Engine is being developed as a production-oriented Spring Boot backend application. The project focuses on clean architecture, maintainability, and incremental feature delivery while following established software engineering practices.

---

## Current Progress

### Completed

- Spring Boot project bootstrap
- Layered project architecture
- Spring Security configuration
- Externalized application configuration
- System Information REST endpoint
- PostgreSQL integration

### In Progress

- Notification domain model
- Persistence layer
- Notification REST API

---

## Architecture

The project follows a layered architecture.

```text
API
 │
 ▼
Application
 │
 ▼
Domain
 │
 ▼
Infrastructure
```

Further architectural documentation is available in the `docs/` directory.

---

## Technology Stack

| Category | Technology |
|----------|------------|
| Language | Java 25 |
| Framework | Spring Boot 3.5 |
| Security | Spring Security |
| Persistence | Spring Data JPA |
| Database | PostgreSQL |
| Build Tool | Maven |

---

## Running the Application

### Prerequisites

- Java 25
- Maven
- PostgreSQL

### Start

```bash
./mvnw spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

Verify the application:

```http
GET /api/v1/system/info
```

---

## Documentation

Project documentation is maintained under the `docs/` directory.

```text
docs/
├── adr/
├── architecture/
└── diagrams/
```

---

## Development

The project is being developed incrementally.

Upcoming milestones include:

- Notification entity
- Database persistence
- REST API
- Validation
- Exception handling
- Email delivery
- SMS delivery
- Retry mechanism
- Scheduling
- Asynchronous processing

---

## License

This repository currently does not specify a license.