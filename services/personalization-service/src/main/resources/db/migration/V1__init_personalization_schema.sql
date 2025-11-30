-- Personalization Service Schema
-- Stores user interest profiles, Tala questions, and personalization data

CREATE SCHEMA IF NOT EXISTS personalization;

-- User Interest Profile Table
-- Tracks user's daily interaction patterns and topic interests
CREATE TABLE personalization.user_interest_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    
    -- Interest vector (JSON: topic -> score 0.0-1.0)
    interest_vector JSONB NOT NULL DEFAULT '{}',
    
    -- Explicit tracking topics (user selected)
    explicit_topics TEXT[] DEFAULT '{}',
    
    -- Recent topics (last 7 days)
    recent_topics TEXT[] DEFAULT '{}',
    
    -- Daily interaction score (0-100)
    daily_interaction_score INTEGER DEFAULT 0,
    
    -- Last updated
    last_interaction_at TIMESTAMP,
    
    -- Metadata
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, profile_id)
);

CREATE INDEX idx_interest_profiles_user ON personalization.user_interest_profiles(user_id);
CREATE INDEX idx_interest_profiles_profile ON personalization.user_interest_profiles(profile_id);
CREATE INDEX idx_interest_profiles_updated ON personalization.user_interest_profiles(updated_at);

-- Daily Interaction Log
-- Tracks daily user interactions for interest calculation
CREATE TABLE personalization.daily_interactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    interaction_date DATE NOT NULL,
    
    -- Interaction types
    chat_count INTEGER DEFAULT 0,
    event_count INTEGER DEFAULT 0,
    timeline_views INTEGER DEFAULT 0,
    insights_views INTEGER DEFAULT 0,
    
    -- Topics discussed
    topics_discussed TEXT[] DEFAULT '{}',
    
    -- Time spent (seconds)
    time_spent_seconds INTEGER DEFAULT 0,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(user_id, profile_id, interaction_date)
);

CREATE INDEX idx_daily_interactions_user_date ON personalization.daily_interactions(user_id, interaction_date);
CREATE INDEX idx_daily_interactions_profile_date ON personalization.daily_interactions(profile_id, interaction_date);

-- Tala Question Bank
-- Stores all available Tala conversation starters
CREATE TABLE personalization.tala_questions (
    id BIGSERIAL PRIMARY KEY,
    
    -- Question details
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL, -- ask_baby_about/daytime_checkin/conversation_starter
    
    -- Categorization
    topic VARCHAR(50) NOT NULL, -- food/sleep/health/development/social/activity
    category VARCHAR(50), -- specific category within topic
    
    -- Age appropriateness
    min_age_months INTEGER NOT NULL DEFAULT 0,
    max_age_months INTEGER, -- NULL means no upper limit
    
    -- Context requirements
    requires_daycare BOOLEAN DEFAULT FALSE,
    requires_incident BOOLEAN DEFAULT FALSE,
    requires_event_type VARCHAR(50), -- specific event type needed
    
    -- Answer configuration
    answer_type VARCHAR(50) NOT NULL, -- choice/scale/boolean/text/number
    answer_choices JSONB, -- For choice type questions
    
    -- Priority and frequency
    base_priority INTEGER DEFAULT 50, -- 0-100
    max_frequency_days INTEGER DEFAULT 7, -- Don't ask more than once per N days
    
    -- Metadata
    tags TEXT[] DEFAULT '{}',
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tala_questions_topic ON personalization.tala_questions(topic);
CREATE INDEX idx_tala_questions_type ON personalization.tala_questions(question_type);
CREATE INDEX idx_tala_questions_age ON personalization.tala_questions(min_age_months, max_age_months);
CREATE INDEX idx_tala_questions_active ON personalization.tala_questions(is_active);

-- User Question History
-- Tracks which questions have been asked to which users
CREATE TABLE personalization.user_question_history (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    question_id BIGINT NOT NULL REFERENCES personalization.tala_questions(id),
    
    -- When asked
    asked_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    asked_date DATE NOT NULL,
    
    -- User response
    answered BOOLEAN DEFAULT FALSE,
    answer_value TEXT,
    answer_metadata JSONB,
    
    -- Context when asked
    context_data JSONB,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_question_history_user ON personalization.user_question_history(user_id, profile_id);
CREATE INDEX idx_question_history_question ON personalization.user_question_history(question_id);
CREATE INDEX idx_question_history_date ON personalization.user_question_history(asked_date);

-- Topic Trends
-- Stores calculated trends for topics
CREATE TABLE personalization.topic_trends (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    topic VARCHAR(50) NOT NULL,
    
    -- Trend data
    trend_direction VARCHAR(20), -- improving/declining/stable
    trend_score DECIMAL(5,2), -- -100 to +100
    
    -- Time period
    period_start DATE NOT NULL,
    period_end DATE NOT NULL,
    
    -- Supporting data
    event_count INTEGER DEFAULT 0,
    avg_score DECIMAL(5,2),
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE(profile_id, topic, period_end)
);

CREATE INDEX idx_topic_trends_profile ON personalization.topic_trends(profile_id);
CREATE INDEX idx_topic_trends_topic ON personalization.topic_trends(topic);
CREATE INDEX idx_topic_trends_period ON personalization.topic_trends(period_end);

-- Insert default Tala questions
INSERT INTO personalization.tala_questions (question_text, question_type, topic, category, min_age_months, max_age_months, answer_type, answer_choices, base_priority, tags) VALUES
-- Ask Baby About - Food (18+ months)
('What did you have for lunch today?', 'ask_baby_about', 'food', 'meal_discussion', 18, NULL, 'text', NULL, 70, ARRAY['conversation', 'daily']),
('Did you like your snack?', 'ask_baby_about', 'food', 'preference', 18, 36, 'choice', '["Loved it", "It was okay", "Didn''t like it"]', 60, ARRAY['conversation', 'preference']),
('What was your favorite food today?', 'ask_baby_about', 'food', 'preference', 24, NULL, 'text', NULL, 65, ARRAY['conversation', 'preference']),

-- Ask Baby About - Friends/Social (24+ months)
('Who did you play with today?', 'ask_baby_about', 'social', 'friend', 24, NULL, 'text', NULL, 75, ARRAY['conversation', 'social']),
('What games did you play with your friends?', 'ask_baby_about', 'activity', 'play', 24, NULL, 'text', NULL, 70, ARRAY['conversation', 'social', 'activity']),
('Did you share toys today?', 'ask_baby_about', 'social', 'sharing', 24, 48, 'choice', '["Yes, I shared!", "No", "A little bit"]', 60, ARRAY['behavior', 'social']),

-- Ask Baby About - Activities (18+ months)
('What did you do at school today?', 'ask_baby_about', 'activity', 'daycare', 18, NULL, 'text', NULL, 80, ARRAY['conversation', 'daycare']),
('Did you do any art today?', 'ask_baby_about', 'activity', 'creative', 18, NULL, 'choice', '["Yes!", "No", "I don''t remember"]', 55, ARRAY['activity', 'creative']),
('Did you go outside to play?', 'ask_baby_about', 'activity', 'outdoor', 18, NULL, 'boolean', NULL, 60, ARRAY['activity', 'outdoor']),

-- Ask Baby About - Incidents (requires incident)
('What happened today? Are you okay?', 'ask_baby_about', 'health', 'incident', 24, NULL, 'text', NULL, 95, ARRAY['incident', 'concern']),
('Does it still hurt?', 'ask_baby_about', 'health', 'pain', 24, NULL, 'choice', '["Yes", "A little", "No, I''m fine"]', 90, ARRAY['incident', 'health']),

-- Daytime Check-in - Health
('How is baby feeling today?', 'daytime_checkin', 'health', 'wellness', 0, NULL, 'choice', '["Great", "Good", "Okay", "Not well"]', 85, ARRAY['daily', 'health']),
('Any signs of teething?', 'daytime_checkin', 'health', 'teething', 4, 24, 'choice', '["Yes, fussy", "Maybe", "No"]', 70, ARRAY['health', 'development']),
('Did baby take medication today?', 'daytime_checkin', 'health', 'medication', 0, NULL, 'boolean', NULL, 75, ARRAY['health', 'tracking']),

-- Daytime Check-in - Sleep
('How was naptime today?', 'daytime_checkin', 'sleep', 'nap', 0, 48, 'choice', '["Great nap", "Short nap", "Skipped nap", "Struggled"]', 80, ARRAY['daily', 'sleep']),
('How did baby sleep last night?', 'daytime_checkin', 'sleep', 'night', 0, NULL, 'choice', '["Slept through", "Few wakes", "Rough night"]', 85, ARRAY['daily', 'sleep']),

-- Daytime Check-in - Food
('How was baby''s appetite today?', 'daytime_checkin', 'food', 'appetite', 0, NULL, 'choice', '["Great", "Normal", "Poor", "Refused food"]', 80, ARRAY['daily', 'food']),
('Any new foods tried today?', 'daytime_checkin', 'food', 'new_food', 6, 36, 'text', NULL, 65, ARRAY['food', 'development']),
('How much milk/formula today?', 'daytime_checkin', 'food', 'milk', 0, 18, 'number', NULL, 75, ARRAY['daily', 'food', 'tracking']),

-- Daytime Check-in - Potty
('Any potty successes today?', 'daytime_checkin', 'potty', 'training', 18, 48, 'choice', '["Yes!", "Tried", "No", "Had accident"]', 70, ARRAY['potty', 'training']),
('How many diapers today?', 'daytime_checkin', 'potty', 'diaper', 0, 36, 'number', NULL, 60, ARRAY['daily', 'tracking']),

-- Daytime Check-in - Behavior/Mood
('What was baby''s mood today?', 'daytime_checkin', 'mood', 'general', 0, NULL, 'choice', '["Happy", "Calm", "Fussy", "Cranky", "Energetic"]', 75, ARRAY['daily', 'mood']),
('Any tantrums today?', 'daytime_checkin', 'mood', 'tantrum', 12, 48, 'choice', '["None", "Minor", "Major meltdown"]', 70, ARRAY['behavior', 'mood']),

-- Daytime Check-in - Development
('Any new words or sounds today?', 'daytime_checkin', 'development', 'language', 6, 36, 'text', NULL, 65, ARRAY['development', 'milestone']),
('Did baby try any new skills today?', 'daytime_checkin', 'development', 'milestone', 0, 48, 'text', NULL, 70, ARRAY['development', 'milestone']);

-- Update trigger for updated_at
CREATE OR REPLACE FUNCTION personalization.update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_user_interest_profiles_updated_at BEFORE UPDATE ON personalization.user_interest_profiles FOR EACH ROW EXECUTE FUNCTION personalization.update_updated_at_column();
CREATE TRIGGER update_daily_interactions_updated_at BEFORE UPDATE ON personalization.daily_interactions FOR EACH ROW EXECUTE FUNCTION personalization.update_updated_at_column();
CREATE TRIGGER update_tala_questions_updated_at BEFORE UPDATE ON personalization.tala_questions FOR EACH ROW EXECUTE FUNCTION personalization.update_updated_at_column();
CREATE TRIGGER update_topic_trends_updated_at BEFORE UPDATE ON personalization.topic_trends FOR EACH ROW EXECUTE FUNCTION personalization.update_updated_at_column();
