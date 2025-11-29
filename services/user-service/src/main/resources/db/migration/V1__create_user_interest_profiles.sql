-- Create user_interest_profiles table
CREATE TABLE IF NOT EXISTS users.user_interest_profiles (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    interest_vector JSONB,
    explicit_topics JSONB,
    recent_topics JSONB,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_user_profile UNIQUE (user_id, profile_id)
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_user_interest_profiles_user_profile 
    ON users.user_interest_profiles(user_id, profile_id);

-- Add comment
COMMENT ON TABLE users.user_interest_profiles IS 'User interest profiles for personalization';
