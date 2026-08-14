ALTER TABLE outbox_events
    ADD COLUMN retry_count INTEGER;

ALTER TABLE outbox_events
    ADD COLUMN next_attempt_at TIMESTAMPTZ;

ALTER TABLE outbox_events
    ADD COLUMN last_failure_reason VARCHAR(500);

UPDATE outbox_events
SET
    retry_count = 0,
    next_attempt_at = created_at;

ALTER TABLE outbox_events
    ALTER COLUMN retry_count SET NOT NULL;

ALTER TABLE outbox_events
    ALTER COLUMN next_attempt_at SET NOT NULL;

ALTER TABLE outbox_events
    ADD CONSTRAINT chk_outbox_retry_count
        CHECK (retry_count >= 0);

CREATE INDEX idx_outbox_pending_next_attempt
    ON outbox_events (
                      status,
                      next_attempt_at
        );