-- AI Service Migration V1
-- Create ai_today_overviews table in ai schema

CREATE TABLE IF NOT EXISTS ai.ai_today_overviews (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    date DATE NOT NULL,
    summary_sentence TEXT,
    action_suggestion TEXT,
    pill_topics JSONB,
    generated_at TIMESTAMP,
    model_version VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_profile_date UNIQUE (profile_id, date)
);

-- Create indexes
CREATE INDEX idx_ato_profile_date ON ai.ai_today_overviews(profile_id, date);
CREATE INDEX idx_ato_date ON ai.ai_today_overviews(date);
CREATE INDEX idx_ato_generated ON ai.ai_today_overviews(generated_at);

-- Create GIN index for JSONB column
CREATE INDEX idx_ato_pill_topics_gin ON ai.ai_today_overviews USING GIN (pill_topics);

-- Add comments
COMMENT ON TABLE ai.ai_today_overviews IS 'AI-generated daily overview content';
COMMENT ON COLUMN ai.ai_today_overviews.summary_sentence IS 'AI-generated summary for At a Glance section';
COMMENT ON COLUMN ai.ai_today_overviews.action_suggestion IS 'AI-suggested action for parents';
COMMENT ON COLUMN ai.ai_today_overviews.pill_topics IS 'AI-generated pill topics with priorities';
COMMENT ON COLUMN ai.ai_today_overviews.model_version IS 'AI model version used (e.g., gemini-1.5-pro)';
