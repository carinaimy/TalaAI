-- Event Service Migration V1
-- Create events table in events schema

CREATE TABLE IF NOT EXISTS events.events (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP NOT NULL,
    event_data JSONB NOT NULL,
    ai_summary TEXT,
    ai_tags JSONB,
    source VARCHAR(50) DEFAULT 'USER_INPUT',
    priority VARCHAR(20),
    urgency_hours INTEGER,
    risk_level VARCHAR(20),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_events_profile_time ON events.events(profile_id, event_time);
CREATE INDEX idx_events_type ON events.events(event_type);
CREATE INDEX idx_events_user ON events.events(user_id);
CREATE INDEX idx_events_deleted ON events.events(deleted_at);
CREATE INDEX idx_events_priority ON events.events(priority);
CREATE INDEX idx_events_source ON events.events(source);

-- Create GIN index for JSONB columns
CREATE INDEX idx_events_data_gin ON events.events USING GIN (event_data);
CREATE INDEX idx_events_tags_gin ON events.events USING GIN (ai_tags);

-- Add comments
COMMENT ON TABLE events.events IS 'Universal event storage for all event types';
COMMENT ON COLUMN events.events.event_type IS 'FEEDING, SLEEP, DIAPER, INCIDENT, SICKNESS, etc.';
COMMENT ON COLUMN events.events.event_data IS 'Flexible JSONB storage for event-specific data';
COMMENT ON COLUMN events.events.priority IS 'low, medium, high, critical';
COMMENT ON COLUMN events.events.source IS 'USER_INPUT, DAYCARE, API';
