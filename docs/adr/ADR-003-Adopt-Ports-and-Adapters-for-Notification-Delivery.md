# ADR-003: Adopt Ports and Adapters for Notification Delivery

- **Status:** Accepted
- **Date:** 2026-08-03

---

# Context

The Notification Engine must support multiple notification delivery channels, including Email, SMS, and Push Notifications.

As the system evolves, additional channels such as WhatsApp, Slack, Firebase Cloud Messaging (FCM), or Kafka-based delivery may be introduced.

The application service should coordinate the notification workflow without containing channel-specific delivery logic.

Without a dedicated abstraction, the service would need conditional logic (`if/else` or `switch`) to determine how notifications are delivered, making the service tightly coupled to infrastructure implementations.

---

# Decision

The application adopts the **Ports and Adapters (Hexagonal Architecture)** pattern for notification delivery.

A domain port named `NotificationChannel` defines the delivery contract.

Each delivery mechanism implements this contract independently.

A `NotificationChannelResolver` is responsible for resolving a `NotificationChannelType` into the appropriate adapter implementation.

The application service depends only on the abstraction and delegates channel selection to the resolver.

```
NotificationService
        │
        ▼
NotificationChannelResolver
        │
 ┌──────┼───────────────┐
 ▼      ▼               ▼
Email   SMS           Push
```

---

# Rationale

This design provides the following benefits:

- Business logic remains independent of delivery implementations.
- New delivery channels can be introduced without modifying `NotificationService`.
- Delivery mechanisms remain isolated and independently testable.
- The design follows the Open/Closed Principle by extending the system through new adapters rather than modifying existing orchestration logic.
- The application layer coordinates workflow while infrastructure concerns remain outside the domain.

---

# Alternatives Considered

## Option 1 — Conditional Logic in NotificationService

Example:

```java
switch (deliveryChannel) {
    case EMAIL -> ...
    case SMS -> ...
    case PUSH -> ...
}
```

### Rejected

Reasons:

- Couples business logic to infrastructure.
- Violates the Open/Closed Principle.
- Requires modifying existing code whenever a new delivery channel is added.
- Reduces maintainability as the number of channels grows.

---

## Option 2 — Reflection or Dynamic Class Loading

### Rejected

Reasons:

- Introduces unnecessary complexity.
- Reduces readability.
- Provides little benefit for the current system size.

---

## Option 3 — Ports and Adapters

### Accepted

Reasons:

- Explicit separation between business logic and infrastructure.
- Easy to extend.
- Easy to unit test.
- Aligns with the project's architectural principles.

---

# Consequences

## Positive

- Extensible notification delivery architecture.
- Clear separation of responsibilities.
- Independent delivery implementations.
- Improved maintainability.
- Reduced coupling.
- Better testability.

## Negative

- Introduces additional classes.
- Requires a resolver component.
- Slightly higher architectural complexity compared to direct conditional logic.

---

# Related Components

- NotificationService
- NotificationChannel
- NotificationChannelResolver
- EmailNotificationChannel
- SmsNotificationChannel
- PushNotificationChannel

---

# Future Considerations

Future delivery channels should implement `NotificationChannel` and register as Spring components.

No changes should be required in `NotificationService` when introducing additional channels.
