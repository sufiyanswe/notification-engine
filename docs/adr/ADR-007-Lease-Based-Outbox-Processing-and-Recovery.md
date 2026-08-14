# ADR-007: Introduce Lease-Based Outbox Processing and Recovery

- **Status:** Accepted

## Context

The outbox worker claims pending events before processing them.

A worker may successfully claim an event and transition it to
`PROCESSING`, but the application can then crash, restart, or become
unavailable before the event reaches a terminal state.

Without a recovery mechanism, such an event could remain permanently
stuck in `PROCESSING` and would no longer be eligible for normal
processing.

The system therefore needs a mechanism to distinguish actively
processing events from abandoned processing attempts.

The recovery mechanism must:

- associate a time-limited lease with a processing attempt,
- prevent an active lease from being recovered prematurely,
- make expired processing attempts eligible for processing again,
- work safely when multiple workers attempt recovery concurrently,
- and preserve the existing outbox state machine.

## Decision

Introduce a lease-based processing mechanism for `OutboxEvent`.

When an event is claimed for processing:

1. The event transitions from `PENDING` to `PROCESSING`.
2. A `leaseUntil` timestamp is assigned to the event.
3. The event remains in `PROCESSING` for the duration of the current
   processing lease, unless processing completes or the event is
   otherwise transitioned out of `PROCESSING`.

The lease duration is configurable through application configuration.

An event in `PROCESSING` is considered recoverable only when its
`leaseUntil` timestamp has passed.

A dedicated recovery service periodically searches for expired
`PROCESSING` events and transitions them back to `PENDING`.

Recovered events receive a new `nextAttemptAt` value and have their
processing lease cleared.

Recovery uses database row-level locking with `FOR UPDATE SKIP LOCKED`
so that concurrent workers cannot recover the same event at the same
time.

The lease is cleared whenever the event leaves `PROCESSING`, including:

- successful processing,
- scheduled retry,
- terminal failure,
- lease recovery.

The lease mechanism is responsible only for recovering abandoned
processing attempts. It does not determine whether a delivery failure
should be retried. Retry decisions remain the responsibility of the
retry policy.

## Consequences

### Positive

- Abandoned `PROCESSING` events can recover automatically.
- A worker crash no longer permanently strands an outbox event.
- Lease expiration provides a deterministic recovery boundary.
- Concurrent recovery attempts are safely coordinated by the database.
- Processing state is represented explicitly in persisted state.
- Recovery remains separate from normal delivery and retry decisions.

### Trade-offs

- Lease duration must be chosen carefully.
- A lease that is too short can cause an active processing attempt to
  be recovered prematurely.
- A lease that is too long delays recovery after a worker failure.
- Recovery introduces additional database queries and background work.
- Lease-based recovery does not provide exactly-once delivery semantics.

## Alternatives Considered

### Permanent `PROCESSING` State

Rejected because a worker failure could leave events permanently stuck
and require manual database intervention.

### Immediate Recovery of All `PROCESSING` Events

Rejected because an event may still be actively processing. Recovering
it immediately could result in concurrent processing of the same event.

### Application-Memory Tracking

Rejected because processing state would be lost when the application
restarts and would not work reliably across multiple application
instances.

### Database Lease with Expiration

Accepted because the lease is persisted with the outbox event and can
be evaluated consistently by any worker instance.

Database row-level locking with `SKIP LOCKED` also allows concurrent
workers to recover different expired events without waiting on each
other.

## Deferred Decisions

The following capabilities remain outside the scope of this decision:

- Distributed worker coordination beyond database row-level locking.
- Explicit worker ownership or worker identity tracking.
- Heartbeat-based lease extension for long-running deliveries.
- Dead-letter queue processing.
- Operational metrics and alerting for repeated lease expiration.

## Reliability Consideration

Lease-based recovery provides at-least-once processing semantics.

If a worker successfully delivers a notification to an external
provider but crashes before the database transaction records the
successful result, the event may later be recovered and delivered again.

Therefore, the lease mechanism prevents permanently abandoned work but
does not guarantee exactly-once external delivery.