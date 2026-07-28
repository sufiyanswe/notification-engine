# Request Flow

## Overview

This document describes how an HTTP request flows through the Notification Engine.

The current implementation uses the `System Information` endpoint to demonstrate the complete request lifecycle.

---

# Request Lifecycle

```
Browser / HTTP Client
        │
        ▼
Tomcat (Embedded Web Server)
        │
        ▼
DispatcherServlet
        │
        ▼
SystemInfoController
        │
        ▼
SystemInfoService
        │
        ▼
ApplicationProperties
        │
        ▼
SystemInfoResponse
        │
        ▼
Jackson
        │
        ▼
HTTP Response (JSON)
```

---

# Step-by-Step Flow

## 1. HTTP Request

A client sends an HTTP request.

```http
GET /api/v1/system/info
```

The request is received by the embedded Tomcat server.

---

## 2. DispatcherServlet

Spring MVC's `DispatcherServlet` receives the request.

Responsibilities:

- Match the request URL
- Locate the appropriate controller
- Invoke the controller method

For this request:

```
GET /api/v1/system/info
```

Spring routes the request to:

```
SystemInfoController.info()
```

---

## 3. Controller

```
SystemInfoController
```

Responsibility:

- Receive the HTTP request
- Delegate work to the application layer
- Return the response

The controller contains no business logic.

```
Client
    │
    ▼
Controller
    │
    ▼
Application Service
```

---

## 4. Application Service

```
SystemInfoService
```

Responsibility:

- Execute the "Get System Information" use case

The service retrieves application metadata from:

```
ApplicationProperties
```

It then creates:

```
SystemInfoResponse
```

---

## 5. Configuration

```
ApplicationProperties
```

Spring binds configuration values from:

```
application.properties
```

Example:

```properties
application.name=notification-engine
application.version=0.1.0-SNAPSHOT
```

These values are injected into the service through constructor injection.

---

## 6. Response Model

The application service returns an immutable response model.

```
SystemInfoResponse
```

The response contains:

- Application name
- Application version
- Status
- Timestamp

---

## 7. JSON Serialization

Spring Boot automatically uses Jackson to serialize the response object into JSON.

Example response:

```json
{
  "application": "notification-engine",
  "version": "0.1.0-SNAPSHOT",
  "status": "UP",
  "timestamp": "2026-07-22T15:27:34.815443400Z"
}
```

---

## 8. HTTP Response

The serialized JSON is returned to the client.

The request lifecycle is now complete.

---

# Layer Interaction

The request travels through the application using the following dependency chain.

```
API
│
▼
Application
│
▼
Infrastructure
```

Current flow:

```
SystemInfoController
        │
        ▼
SystemInfoService
        │
        ▼
ApplicationProperties
```

Each layer performs a single responsibility before delegating to the next layer.

---

# Engineering Notes

This endpoint intentionally demonstrates the project's architectural rules.

- Controllers remain thin.
- Business logic resides in the application layer.
- Configuration is accessed through `ApplicationProperties`.
- Response models are immutable.
- Dependencies follow the project's layered architecture.

Future endpoints should follow the same request lifecycle.