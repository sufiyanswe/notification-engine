# Request Flow

## Overview

This document describes the runtime execution flow of a notification request through the Notification Engine.

The objective is to show how an HTTP request is processed from the API layer to persistence and back to the client.

---

# High-Level Flow

```text
Client
    │
    ▼
HTTP Request
    │
    ▼
NotificationController
    │
    ▼
Bean Validation
    │
    ▼
NotificationService
    │
    ├────────────────────────────┐
    ▼                            ▼
NotificationRepository      NotificationChannelResolver
    │                            │
    │                            ▼
    │                    NotificationChannel
    │                            │
    │              ┌─────────────┼──────────────┐
    │              ▼             ▼              ▼
    │            Email          SMS           Push
    │
    ▼
Hibernate Persistence Context
    │
    ▼
Dirty Checking
    │
    ▼
PostgreSQL
    │
    ▼
NotificationResponse
    │
    ▼
HTTP 201 Created
```

---

# Request Lifecycle

## 1. Client Request

The client submits an HTTP POST request to create a notification.

Example endpoint:

```
POST /api/v1/notifications
```

---

## 2. API Layer

`NotificationController` receives the request.

Responsibilities:

- Deserialize JSON
- Validate request
- Invoke the application service

No business logic is executed here.

---

## 3. Application Layer

`NotificationService` coordinates the complete use case.

Responsibilities:

- Create the notification
- Persist the notification
- Resolve the delivery channel
- Delegate notification delivery
- Update notification state
- Return the result

---

## 4. Persistence

The notification is persisted using the repository.

After persistence, the entity becomes managed by Hibernate.

---

## 5. Delivery

The application delegates delivery to the resolver.

The resolver selects the correct implementation based on the notification channel.

Examples:

- EmailNotificationChannel
- SmsNotificationChannel
- PushNotificationChannel

---

## 6. Domain State Transition

If delivery succeeds, the notification updates its own state.

```
PENDING
    │
    ▼
SENT
```

The state transition is enforced by the domain model.

---

## 7. Transaction Commit

The application transaction completes.

Hibernate detects entity changes through Dirty Checking and synchronizes them with the database.

No additional save operation is required after the state transition.

---

## 8. Response

The controller returns a response containing:

- Notification ID
- Recipient ID
- Title
- Message
- Status
- Creation Timestamp

HTTP Status:

```
201 Created
```

---

# Design Characteristics

The request flow demonstrates:

- Thin Controllers
- Application Service orchestration
- Rich Domain Model
- Ports and Adapters
- Repository Pattern
- Hibernate Dirty Checking
- Transactional consistency