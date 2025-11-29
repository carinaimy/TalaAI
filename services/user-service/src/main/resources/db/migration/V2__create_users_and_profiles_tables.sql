-- User Service Migration V2
-- Create users and profiles tables in users schema

-- Users table
CREATE TABLE IF NOT EXISTS users.users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users.users(email);
CREATE INDEX idx_users_deleted ON users.users(deleted_at);

COMMENT ON TABLE users.users IS 'User accounts';
COMMENT ON COLUMN users.users.password_hash IS 'BCrypt hashed password';

-- Profiles table (Baby profiles)
CREATE TABLE IF NOT EXISTS users.profiles (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    baby_name VARCHAR(255) NOT NULL,
    birth_date DATE,
    timezone VARCHAR(50) DEFAULT 'UTC',
    gender VARCHAR(20),
    photo_url TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_profiles_user_id ON users.profiles(user_id);
CREATE INDEX idx_profiles_birth_date ON users.profiles(birth_date);
CREATE INDEX idx_profiles_deleted ON users.profiles(deleted_at);

COMMENT ON TABLE users.profiles IS 'Baby/child profiles';
COMMENT ON COLUMN users.profiles.baby_name IS 'Child name';
COMMENT ON COLUMN users.profiles.birth_date IS 'Date of birth for age calculation';
