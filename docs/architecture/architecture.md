# Architecture

## Overview

Notification Engine follows a layered architecture that separates HTTP concerns, application use cases, business concepts, and infrastructure.

The primary objective of this architecture is to keep responsibilities isolated, reduce coupling between components, and allow the system to evolve without forcing unrelated parts of the codebase to change.

---

# Architectural Goals

The architecture is designed around the following goals:

- Clear separation of concerns
- Maintainable codebase
- Explicit dependency direction
- Replaceable infrastructure
- Testable business logic
- Incremental feature development

Every new feature introduced into the project should preserve these goals.

---

# Layer Overview

```
                    HTTP Request
                          │
                          ▼
                   API Layer
                          │
                          ▼
              Application Layer
                          │
                          ▼
                 Domain Layer
                          │
                          ▼
             Infrastructure Layer
```

Dependencies always point downward.

No layer should depend on a higher layer.

---

# Layers

## API

**Responsibility**

Expose HTTP endpoints and translate HTTP requests into application use cases.

Contains:

- Controllers
- Request models
- Response models

The API layer should never contain business logic.

Current package:

```
api/
```

---

## Application

**Responsibility**

Execute application use cases.

Application services coordinate work between the domain model and infrastructure without exposing HTTP or persistence concerns.

Contains:

- Application services
- Use case orchestration

Current package:

```
application/
```

---

## Domain

**Responsibility**

Represent the core business concepts of the Notification Engine.

The domain should remain independent from Spring Framework and infrastructure technologies whenever practical.

Contains:

- Domain models
- Domain abstractions
- Repository interfaces

Current package:

```
domain/
```

---

## Infrastructure

**Responsibility**

Provide technical capabilities required by the application.

Infrastructure implements communication with external systems.

Examples include:

- Database access
- Configuration
- Messaging
- Email providers
- External APIs

Current package:

```
infrastructure/
```

---

# Dependency Rules

The project follows the following dependency direction.

```
API
    ↓
Application
    ↓
Domain
    ↓
Infrastructure
```

Allowed dependencies:

- API → Application
- Application → Domain
- Application → Infrastructure
- Infrastructure → External Systems

The following dependencies are not allowed:

- Domain → Spring MVC
- Domain → Controllers
- API → Database
- Controllers → Repositories
- Infrastructure → Controllers

---

# Current Implementation

As of the current milestone, the architecture includes:

```
NotificationEngineApplication

API
 ├── SystemInfoController
 └── SystemInfoResponse

Application
 └── SystemInfoService

Infrastructure
 ├── SecurityConfig
 └── ApplicationProperties

Domain
 └── (Reserved for notification model)
```

The System Information endpoint demonstrates the complete request lifecycle while validating the layered architecture.

---

# Design Principles

The project currently follows these principles:

- Single Responsibility Principle
- Constructor-based Dependency Injection
- Thin Controllers
- Immutable Response Models
- Externalized Configuration
- Explicit Package Organization
- Layered Architecture

These principles should be preserved as the project evolves.

---

# Future Evolution

The architecture is intentionally designed to support future additions without requiring structural changes.

Planned additions include:

- Notification domain model
- Repository implementations
- Email provider integration
- SMS provider integration
- Retry mechanism
- Scheduling
- Asynchronous processing
- Monitoring
- Metrics