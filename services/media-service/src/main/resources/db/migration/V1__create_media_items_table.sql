-- Media Service Migration V1
-- Create media_items table in media schema

CREATE TABLE IF NOT EXISTS media.media_items (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    user_id BIGINT,
    care_provider_id BIGINT,
    source VARCHAR(50) DEFAULT 'USER_UPLOADED',
    media_type VARCHAR(20) NOT NULL,
    storage_url TEXT NOT NULL,
    thumbnail_url TEXT,
    occurred_at TIMESTAMP,
    ai_tags JSONB,
    faces_count INTEGER,
    emotion_score JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_media_profile_occurred ON media.media_items(profile_id, occurred_at);
CREATE INDEX idx_media_type ON media.media_items(media_type);
CREATE INDEX idx_media_source ON media.media_items(source);
CREATE INDEX idx_media_user ON media.media_items(user_id);
CREATE INDEX idx_media_care_provider ON media.media_items(care_provider_id);
CREATE INDEX idx_media_deleted ON media.media_items(deleted_at);

-- Create GIN index for JSONB columns
CREATE INDEX idx_media_tags_gin ON media.media_items USING GIN (ai_tags);
CREATE INDEX idx_media_emotion_gin ON media.media_items USING GIN (emotion_score);

-- Add comments
COMMENT ON TABLE media.media_items IS 'Photo and video storage metadata';
COMMENT ON COLUMN media.media_items.media_type IS 'PHOTO, VIDEO';
COMMENT ON COLUMN media.media_items.source IS 'USER_UPLOADED, DAYCARE_EMAIL, DAYCARE_API';
COMMENT ON COLUMN media.media_items.occurred_at IS 'When photo/video was taken (may differ from created_at)';
COMMENT ON COLUMN media.media_items.emotion_score IS 'AI-detected emotions: {"happy": 0.9, "sad": 0.1}';
