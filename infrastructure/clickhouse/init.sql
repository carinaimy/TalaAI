-- Tala Backend ClickHouse Initialization
-- Create database
CREATE DATABASE IF NOT EXISTS tala_analytics;

-- Create events_analytics table
CREATE TABLE IF NOT EXISTS tala_analytics.events_analytics (
    id UInt64,
    profile_id UInt64,
    user_id UInt64,
    event_type LowCardinality(String),
    event_time DateTime64(3, 'UTC'),
    event_date Date,
    age_in_months UInt16,
    
    -- Common fields (nullable for flexibility)
    duration_minutes Nullable(UInt32),
    amount Nullable(Float32),
    unit Nullable(String),
    quality Nullable(String),
    mood Nullable(String),
    
    -- Type-specific fields
    feeding_type Nullable(String),
    food_name Nullable(String),
    sleep_location Nullable(String),
    diaper_type Nullable(String),
    medication_name Nullable(String),
    
    -- AI fields
    ai_summary String,
    ai_tags Array(String),
    
    -- Metadata
    created_at DateTime64(3, 'UTC'),
    source LowCardinality(String)
    
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_date)
ORDER BY (profile_id, event_type, event_time)
SETTINGS index_granularity = 8192;

-- Create materialized view for daily summary
CREATE MATERIALIZED VIEW IF NOT EXISTS tala_analytics.daily_summary_mv
ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(event_date)
ORDER BY (profile_id, event_date, event_type)
AS SELECT
    profile_id,
    event_date,
    event_type,
    count() AS event_count,
    avg(duration_minutes) AS avg_duration,
    avg(amount) AS avg_amount,
    groupArray(10)(mood) AS moods,
    max(created_at) AS last_updated
FROM tala_analytics.events_analytics
GROUP BY profile_id, event_date, event_type;

-- Create materialized view for weekly patterns
CREATE MATERIALIZED VIEW IF NOT EXISTS tala_analytics.weekly_patterns_mv
ENGINE = AggregatingMergeTree()
PARTITION BY toYYYYMM(event_date)
ORDER BY (profile_id, event_type, dayOfWeek(event_date), toHour(event_time))
AS SELECT
    profile_id,
    event_type,
    event_date,
    dayOfWeek(event_date) AS day_of_week,
    toHour(event_time) AS hour_of_day,
    countState() AS event_count,
    avgState(duration_minutes) AS avg_duration,
    avgState(amount) AS avg_amount
FROM tala_analytics.events_analytics
GROUP BY profile_id, event_type, event_date, day_of_week, hour_of_day;

-- Create table for AI insights
CREATE TABLE IF NOT EXISTS tala_analytics.ai_insights (
    id UInt64,
    profile_id UInt64,
    insight_type LowCardinality(String),
    insight_category LowCardinality(String),
    title String,
    description String,
    confidence Float32,
    data_points UInt32,
    date_range_start Date,
    date_range_end Date,
    metadata String,
    created_at DateTime64(3, 'UTC'),
    expires_at Nullable(DateTime64(3, 'UTC'))
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(created_at)
ORDER BY (profile_id, insight_type, created_at)
SETTINGS index_granularity = 8192;

-- Create table for pattern detection
CREATE TABLE IF NOT EXISTS tala_analytics.patterns (
    id UInt64,
    profile_id UInt64,
    pattern_type LowCardinality(String),
    pattern_name String,
    frequency LowCardinality(String),
    confidence Float32,
    occurrences Array(DateTime64(3, 'UTC')),
    metadata String,
    detected_at DateTime64(3, 'UTC'),
    last_seen DateTime64(3, 'UTC')
) ENGINE = ReplacingMergeTree(last_seen)
PARTITION BY toYYYYMM(detected_at)
ORDER BY (profile_id, pattern_type, pattern_name)
SETTINGS index_granularity = 8192;
