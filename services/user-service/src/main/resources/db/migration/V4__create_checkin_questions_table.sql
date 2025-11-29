-- User Service Migration V4
-- Create checkin questions table

CREATE TABLE IF NOT EXISTS users.checkin_questions (
    id BIGINT PRIMARY KEY,
    age_min_months INTEGER,
    age_max_months INTEGER,
    topic VARCHAR(50),
    question_text TEXT NOT NULL,
    answer_type VARCHAR(20),
    choices JSONB,
    frequency_hint VARCHAR(20),
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_cq_age_range ON users.checkin_questions(age_min_months, age_max_months);
CREATE INDEX idx_cq_topic ON users.checkin_questions(topic);
CREATE INDEX idx_cq_active ON users.checkin_questions(active);

-- Create GIN index for JSONB column
CREATE INDEX idx_cq_choices_gin ON users.checkin_questions USING GIN (choices);

COMMENT ON TABLE users.checkin_questions IS 'Daily checkin questions for different age ranges';
COMMENT ON COLUMN users.checkin_questions.topic IS 'sleep, food, health, development, mood';
COMMENT ON COLUMN users.checkin_questions.answer_type IS 'scale, boolean, text, number, choice';
COMMENT ON COLUMN users.checkin_questions.choices IS 'Array of choices for choice-type questions';
COMMENT ON COLUMN users.checkin_questions.frequency_hint IS 'daily, weekly, monthly';

-- Insert sample questions
INSERT INTO users.checkin_questions (id, age_min_months, age_max_months, topic, question_text, answer_type, choices, frequency_hint, active, created_at, updated_at) VALUES
(1, 0, 6, 'sleep', 'How is the baby sleeping today?', 'choice', '["Great", "Good", "Okay", "Poor"]', 'daily', true, NOW(), NOW()),
(2, 0, 12, 'food', 'How is the baby eating today?', 'choice', '["Great appetite", "Normal", "Picky", "Refusing food"]', 'daily', true, NOW(), NOW()),
(3, 0, 60, 'health', 'How is the baby feeling today?', 'choice', '["Healthy", "Minor cold", "Teething", "Unwell"]', 'daily', true, NOW(), NOW()),
(4, 6, 24, 'development', 'Any new milestones or skills observed?', 'text', NULL, 'weekly', true, NOW(), NOW()),
(5, 0, 60, 'mood', 'What is the baby''s mood today?', 'scale', '["1", "2", "3", "4", "5", "6", "7", "8", "9", "10"]', 'daily', true, NOW(), NOW());
