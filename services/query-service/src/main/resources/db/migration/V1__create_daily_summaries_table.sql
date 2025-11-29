-- Query Service Migration V1
-- Create daily_child_summaries table in analytics schema

CREATE TABLE IF NOT EXISTS analytics.daily_child_summaries (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    date DATE NOT NULL,
    events_summary JSONB,
    metrics JSONB,
    candidate_media_ids JSONB,
    candidate_incident_ids JSONB,
    total_events INTEGER,
    has_incident BOOLEAN,
    has_sickness BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_profile_date UNIQUE (profile_id, date)
);

-- Create indexes
CREATE INDEX idx_dcs_profile_date ON analytics.daily_child_summaries(profile_id, date);
CREATE INDEX idx_dcs_date ON analytics.daily_child_summaries(date);
CREATE INDEX idx_dcs_has_incident ON analytics.daily_child_summaries(has_incident);
CREATE INDEX idx_dcs_has_sickness ON analytics.daily_child_summaries(has_sickness);

-- Create GIN indexes for JSONB columns
CREATE INDEX idx_dcs_events_summary_gin ON analytics.daily_child_summaries USING GIN (events_summary);
CREATE INDEX idx_dcs_metrics_gin ON analytics.daily_child_summaries USING GIN (metrics);

-- Add comments
COMMENT ON TABLE analytics.daily_child_summaries IS 'Daily aggregated summaries per child';
COMMENT ON COLUMN analytics.daily_child_summaries.events_summary IS 'Event counts by type: {"FEEDING": 6, "SLEEP": 3}';
COMMENT ON COLUMN analytics.daily_child_summaries.metrics IS 'Calculated metrics: {"sleep_total_minutes": 720, "avg_mood": 8.5}';
COMMENT ON COLUMN analytics.daily_child_summaries.candidate_media_ids IS 'Top photos for the day';
COMMENT ON COLUMN analytics.daily_child_summaries.candidate_incident_ids IS 'Incidents to highlight';
