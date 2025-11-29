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

-- Note: Tables, indexes, and triggers are created by Flyway migrations
-- This init.sql only sets up extensions, schemas, and permissions

-- Grant table privileges
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA events TO tala;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA users TO tala;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA analytics TO tala;

-- Grant sequence privileges
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA events TO tala;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA users TO tala;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA analytics TO tala;
