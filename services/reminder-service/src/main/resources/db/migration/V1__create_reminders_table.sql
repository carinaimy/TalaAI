-- Reminder Service Migration V1
-- Create reminders table in reminders schema

CREATE TABLE IF NOT EXISTS reminders.reminders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT,
    source_event_id BIGINT,
    category VARCHAR(50) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_at TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE' NOT NULL,
    snooze_until TIMESTAMP,
    recurrence_rule VARCHAR(255),
    priority VARCHAR(20) DEFAULT 'medium',
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_reminders_user_profile ON reminders.reminders(user_id, profile_id);
CREATE INDEX idx_reminders_due ON reminders.reminders(due_at);
CREATE INDEX idx_reminders_status ON reminders.reminders(status);
CREATE INDEX idx_reminders_category ON reminders.reminders(category);
CREATE INDEX idx_reminders_source_event ON reminders.reminders(source_event_id);
CREATE INDEX idx_reminders_deleted ON reminders.reminders(deleted_at);

-- Add comments
COMMENT ON TABLE reminders.reminders IS 'Reminder and notification storage';
COMMENT ON COLUMN reminders.reminders.category IS 'prepare, info, appointment, weather, vaccination';
COMMENT ON COLUMN reminders.reminders.status IS 'ACTIVE, SNOOZED, COMPLETED, CANCELED';
COMMENT ON COLUMN reminders.reminders.source_event_id IS 'Link to triggering event if auto-generated';
COMMENT ON COLUMN reminders.reminders.recurrence_rule IS 'RRULE format for recurring reminders';
