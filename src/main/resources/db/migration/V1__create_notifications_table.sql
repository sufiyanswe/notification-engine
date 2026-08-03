CREATE TABLE notifications (

                               id UUID PRIMARY KEY,

                               recipient_id TEXT NOT NULL,

                               title TEXT,

                               message TEXT NOT NULL,

                               delivery_channel TEXT NOT NULL,

                               status TEXT NOT NULL,

                               created_at TIMESTAMPTZ NOT NULL

);