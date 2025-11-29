-- TalaAI Database Initialization Script
-- Creates all schemas and extensions

-- Enable required extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";  -- For text search

-- Create schemas for each service
CREATE SCHEMA IF NOT EXISTS users;
CREATE SCHEMA IF NOT EXISTS events;
CREATE SCHEMA IF NOT EXISTS reminders;
CREATE SCHEMA IF NOT EXISTS media;
CREATE SCHEMA IF NOT EXISTS files;
CREATE SCHEMA IF NOT EXISTS analytics;
CREATE SCHEMA IF NOT EXISTS ai;

-- Grant permissions to tala user
GRANT ALL PRIVILEGES ON SCHEMA users TO tala;
GRANT ALL PRIVILEGES ON SCHEMA events TO tala;
GRANT ALL PRIVILEGES ON SCHEMA reminders TO tala;
GRANT ALL PRIVILEGES ON SCHEMA media TO tala;
GRANT ALL PRIVILEGES ON SCHEMA files TO tala;
GRANT ALL PRIVILEGES ON SCHEMA analytics TO tala;
GRANT ALL PRIVILEGES ON SCHEMA ai TO tala;

-- Set default privileges for future tables
ALTER DEFAULT PRIVILEGES IN SCHEMA users GRANT ALL ON TABLES TO tala;
ALTER DEFAULT PRIVILEGES IN SCHEMA events GRANT ALL ON TABLES TO tala;
ALTER DEFAULT PRIVILEGES IN SCHEMA reminders GRANT ALL ON TABLES TO tala;
ALTER DEFAULT PRIVILEGES IN SCHEMA media GRANT ALL ON TABLES TO tala;
ALTER DEFAULT PRIVILEGES IN SCHEMA files GRANT ALL ON TABLES TO tala;
ALTER DEFAULT PRIVILEGES IN SCHEMA analytics GRANT ALL ON TABLES TO tala;
ALTER DEFAULT PRIVILEGES IN SCHEMA ai GRANT ALL ON TABLES TO tala;

-- Set search path
ALTER DATABASE tala_db SET search_path TO users, events, reminders, media, files, analytics, ai, public;

-- Create comments
COMMENT ON SCHEMA users IS 'User Service - Users, Profiles, Care Providers, Interest Tracking';
COMMENT ON SCHEMA events IS 'Event Service - Universal Event Storage';
COMMENT ON SCHEMA reminders IS 'Reminder Service - Reminders and Notifications';
COMMENT ON SCHEMA media IS 'Media Service - Photos and Videos';
COMMENT ON SCHEMA files IS 'File Service - File Metadata';
COMMENT ON SCHEMA analytics IS 'Query Service - Daily Summaries and Analytics';
COMMENT ON SCHEMA ai IS 'AI Service - AI-Generated Content';
