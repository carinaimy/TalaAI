-- Tala Backend PostgreSQL Initialization
-- Create databases for development and test
-- Note: CREATE DATABASE IF NOT EXISTS is not supported in PostgreSQL
-- The main database tala_db is created by the POSTGRES_DB env var

-- Connect to tala_db
\c tala_db

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- Create schemas
CREATE SCHEMA IF NOT EXISTS events;
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS analytics;

-- Grant privileges to tala user
GRANT ALL PRIVILEGES ON SCHEMA events TO tala;
GRANT ALL PRIVILEGES ON SCHEMA users TO tala;
GRANT ALL PRIVILEGES ON SCHEMA analytics TO tala;

-- Create events table
CREATE TABLE IF NOT EXISTS events.events (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    event_data JSONB NOT NULL,
    ai_summary TEXT,
    ai_tags JSONB,
    source VARCHAR(50) DEFAULT 'USER_INPUT',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for events table
CREATE INDEX IF NOT EXISTS idx_events_profile_time ON events.events(profile_id, event_time DESC);
CREATE INDEX IF NOT EXISTS idx_events_type ON events.events(event_type);
CREATE INDEX IF NOT EXISTS idx_events_user_id ON events.events(user_id);
CREATE INDEX IF NOT EXISTS idx_events_deleted_at ON events.events(deleted_at) WHERE deleted_at IS NULL;
CREATE INDEX IF NOT EXISTS idx_events_event_data ON events.events USING GIN(event_data);

-- Create users table
CREATE TABLE IF NOT EXISTS users.users (
    id BIGINT PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    phone VARCHAR(50),
    timezone VARCHAR(50) DEFAULT 'UTC',
    locale VARCHAR(10) DEFAULT 'en',
    email_verified BOOLEAN DEFAULT FALSE,
    phone_verified BOOLEAN DEFAULT FALSE,
    subscription_tier VARCHAR(50) DEFAULT 'FREE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create profiles table
CREATE TABLE IF NOT EXISTS users.profiles (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users.users(id),
    name VARCHAR(255) NOT NULL,
    date_of_birth DATE NOT NULL,
    gender VARCHAR(20),
    profile_photo_url TEXT,
    height_cm DECIMAL(5,2),
    weight_kg DECIMAL(5,2),
    metadata JSONB,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    deleted_at TIMESTAMP WITH TIME ZONE
);

-- Create indexes for users and profiles
CREATE INDEX IF NOT EXISTS idx_users_email ON users.users(email);
CREATE INDEX IF NOT EXISTS idx_profiles_user_id ON users.profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_profiles_deleted_at ON users.profiles(deleted_at) WHERE deleted_at IS NULL;

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for automatic updated_at
CREATE TRIGGER update_events_updated_at BEFORE UPDATE ON events.events
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_users_updated_at BEFORE UPDATE ON users.users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_profiles_updated_at BEFORE UPDATE ON users.profiles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Grant table privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA events TO tala;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA users TO tala;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA analytics TO tala;

-- Grant sequence privileges
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA events TO tala;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA users TO tala;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA analytics TO tala;
