# ADR-002: Adopt Layered Architecture

- **Status:** Accepted
- **Date:** 2026-07-22

---

## Context

As the Notification Engine evolves, the application will support multiple notification channels, persistence, scheduling, retries, provider integrations, and operational monitoring.

A package structure organized solely by Spring stereotypes (such as `controller`, `service`, `repository`, and `entity`) tends to mix responsibilities as the project grows. Business logic becomes tightly coupled to framework components, making the codebase harder to understand, maintain, and extend.

The project requires an architectural structure that clearly separates HTTP concerns, application orchestration, business concepts, and infrastructure concerns.

---

## Decision

The project adopts a layered architecture consisting of the following layers:

```
              API
               │
               ▼
         Application
          ↙       ↘
     Domain    Infrastructure
```

Each layer has a clearly defined responsibility.

| Layer | Responsibility |
|--------|----------------|
| API | HTTP endpoints, request models, response models |
| Application | Application use cases and orchestration |
| Domain | Business models and domain abstractions |
| Infrastructure | Configuration, persistence, and external integrations |

Dependency direction is intentionally restricted.

Allowed dependencies:

- API → Application
- Application → Domain
- Application → Infrastructure

The Domain layer should remain independent of Spring Framework whenever practical.

---

## Consequences

### Positive

- Clear separation of concerns
- Explicit dependency boundaries
- Improved maintainability
- Easier testing
- Better scalability as new features are introduced
- Reduced coupling between business logic and infrastructure

### Negative

- Additional packages and classes
- Slightly higher upfront complexity
- More navigation between layers during development

The team accepts these trade-offs in exchange for a more maintainable and extensible architecture.

---

## Related Documents

- ADR-001: Use Spring Security
- `docs/architecture/architecture.md`