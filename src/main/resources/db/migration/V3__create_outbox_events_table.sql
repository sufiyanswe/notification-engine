CREATE TABLE outbox_events (

                               id UUID PRIMARY KEY,

                               notification_id UUID NOT NULL
                                   REFERENCES notifications(id),

                               event_type TEXT NOT NULL,

                               status TEXT NOT NULL,

                               created_at TIMESTAMPTZ NOT NULL,

                               processed_at TIMESTAMPTZ,

                               CONSTRAINT uk_outbox_notification_event
                                   UNIQUE(notification_id, event_type)

);

CREATE INDEX idx_outbox_status_created
    ON outbox_events(status, created_at);