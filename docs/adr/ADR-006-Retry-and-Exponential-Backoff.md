# ADR-006: Introduce Retry Scheduling with Exponential Backoff

- **Status:** Accepted

## Context

The background worker can asynchronously process outbox events, but
delivery attempts can fail because of temporary external failures such
as an unavailable SMTP server or other transient delivery problems.

Treating every delivery failure as a terminal failure would make the
notification system unnecessarily fragile.

The system therefore needs a deterministic mechanism to decide:

- whether a failed delivery should be retried,
- how many times it may be retried,
- when the next attempt should occur,
- and when the failure should become terminal.

Retry scheduling must also remain separate from the delivery mechanism
itself. `OutboxProcessor` should coordinate the notification lifecycle,
while a dedicated retry policy should determine retry timing.

## Decision

Introduce a dedicated `RetryPolicy` responsible for determining whether
a failed delivery should be retried and, if so, calculating the next
attempt time.

The retry policy will use exponential backoff:

`delay = initialDelay × multiplier^(retryCount - 1)`

The retry policy is governed by configuration:

- `maxRetries`
- `initialDelayMs`
- `multiplier`

Retry state is persisted on the `OutboxEvent` through:

- `retryCount`
- `nextAttemptAt`
- `lastFailureReason`

When delivery fails:

1. `OutboxProcessor` passes the delivery failure to `RetryPolicy`.
2. `RetryPolicy` determines whether another attempt is allowed.
3. If retrying is allowed, the event returns to `PENDING`.
4. `nextAttemptAt` determines when the event becomes eligible again.
5. If the retry limit has been exhausted, the failure becomes terminal
   and the notification and outbox event transition to `FAILED`.

The retry policy does not perform delivery and does not modify the
notification lifecycle directly. Its responsibility is limited to
retry decision-making and retry timing.

## Consequences

### Positive

- Temporary delivery failures can be retried automatically.
- Retry timing is persisted with the outbox event, so eligibility
  survives application restarts.
- Exponential backoff reduces repeated immediate attempts against a
  failing external delivery system.
- Retry behavior is configurable without changing application code.
- Retry decision-making remains isolated from notification delivery.
- The outbox remains the source of truth for retry state.

### Trade-offs

- Failed deliveries may remain in the system for multiple attempts
  before becoming terminal.
- Additional database state is required to track retry attempts and
  scheduling.
- Retry configuration directly affects delivery latency.
- Exponential backoff does not guarantee that a transient external
  failure will eventually recover.

## Alternatives Considered

### Fixed Retry Delay

Rejected because every retry would occur after the same delay,
regardless of how many previous attempts had already failed.

This can cause repeated pressure on an already failing external system.

### Immediate Retry

Rejected because repeated failures would result in rapid successive
delivery attempts and could amplify load on external services.

### Message Broker with Built-in Retry

Rejected because introducing a message broker would add infrastructure
complexity that is outside the current scope of the notification engine.

### Retry Logic Inside OutboxProcessor

Rejected because it would couple delivery orchestration with retry
policy and make the processor responsible for two separate concerns.

### Exponential Backoff with Dedicated RetryPolicy

Accepted because it provides predictable retry timing while keeping
retry decision-making isolated from notification delivery.

## Deferred Decisions

The following capabilities are intentionally deferred:

- Dead-letter queue for permanently failed notifications.
- Dynamic retry policies based on failure type or external provider
  behavior.
- Provider-specific retry strategies.
- Distributed retry coordination across multiple worker instances.