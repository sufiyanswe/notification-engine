# Request Flow

This document shows how a notification request currently moves through
Notification Engine, from the HTTP request to durable persistence and
asynchronous delivery.

## 1. Notification Creation

```text
Client
  │
  │ HTTP POST
  ▼
NotificationController
  │
  │ request data
  ▼
NotificationService
  │
  ├── Create Notification
  │
  ├── Create OutboxEvent
  │
  ├── Save Notification
  │
  ├── Save OutboxEvent
  │
  ▼
COMMIT
  │
  ▼
HTTP Response
```

The `Notification` and its corresponding `OutboxEvent` are persisted in
the same transaction.

The HTTP request does not perform external notification delivery.

---

## 2. Background Delivery

After the transaction commits, the outbox event is processed
asynchronously.

```text
OutboxWorker
    │
    ▼
OutboxClaimService
    │
    │ find eligible PENDING events
    │ FOR UPDATE SKIP LOCKED
    ▼
OutboxEvent → PROCESSING
    │
    │ assign leaseUntil
    ▼
OutboxProcessor
    │
    ▼
Load Notification
    │
    ▼
NotificationChannelResolver
    │
    ▼
NotificationChannel
    │
 ┌──┼──────────────┐
 ▼  ▼              ▼
Email SMS          Push
```

An event is eligible for claiming when:

- status is `PENDING`
- `nextAttemptAt <= now`

Claiming changes the event to `PROCESSING` and assigns a processing
lease.

---

## 3. Successful Delivery

```text
OutboxProcessor
      │
      ▼
NotificationChannel
      │
      ▼
DeliveryResult: SUCCESS
      │
      ├── Notification → SENT
      │
      └── OutboxEvent → PROCESSED
```

The processing transaction records the successful delivery state.

---

## 4. Failed Delivery

```text
DeliveryResult: FAILURE
        │
        ▼
    RetryPolicy
        │
   ┌────┴─────┐
   │          │
 Retry      Terminal
   │          │
   ▼          ▼
PENDING     FAILED
```

For a retryable failure, `RetryPolicy` calculates the next attempt time
using exponential backoff.

The outbox event stores:

- `retryCount`
- `nextAttemptAt`
- `lastFailureReason`

The event becomes eligible for another claim when `nextAttemptAt` is
reached.

If no retry is allowed:

```text
Notification → FAILED
OutboxEvent   → FAILED
```

---

## 5. Worker Failure and Recovery

A worker can crash after claiming an event.

```text
PENDING
   │
   │ claim
   ▼
PROCESSING
   │
   │ worker crashes
   ▼
leaseUntil expires
   │
   ▼
OutboxRecoveryService
   │
   ▼
PENDING
```

`OutboxRecoveryWorker` periodically invokes the recovery process.

Expired `PROCESSING` events are safely recovered using database locking.
The lease is cleared and the event becomes eligible for processing again.

---

## Complete Flow

```text
                         HTTP REQUEST
                              │
                              ▼
                    NotificationController
                              │
                              ▼
                    NotificationService
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
              Notification          OutboxEvent
                    │                   │
                    └─────────┬─────────┘
                              ▼
                           COMMIT
                              │
                              ▼
                       HTTP RESPONSE


                    ASYNCHRONOUS PATH
                              │
                              ▼
                        OutboxWorker
                              │
                              ▼
                     OutboxClaimService
                              │
                              ▼
                    PROCESSING + LEASE
                              │
                              ▼
                       OutboxProcessor
                              │
                              ▼
                 NotificationChannelResolver
                              │
                    ┌─────────┼─────────┐
                    ▼         ▼         ▼
                  Email      SMS       Push
                    │         │         │
                    └─────────┼─────────┘
                              ▼
                       DeliveryResult
                              │
                    ┌─────────┴─────────┐
                    ▼                   ▼
                 Success              Failure
                    │                   │
              ┌─────┴─────┐             ▼
              ▼           ▼        RetryPolicy
        Notification    Outbox        │
           SENT        PROCESSED  ┌───┴────┐
                                  ▼        ▼
                               PENDING   FAILED
                                  │
                                  ▼
                            Future Claim


                    RECOVERY PATH
                              │
                              ▼
                    OutboxRecoveryWorker
                              │
                              ▼
                    OutboxRecoveryService
                              │
                              ▼
                    Expired PROCESSING
                              │
                              ▼
                           PENDING
```

## Important Boundary

The request path ends after the notification and outbox event are
successfully persisted.

External notification delivery belongs to the asynchronous processing
path.

This separation is the core of the Transactional Outbox design.
