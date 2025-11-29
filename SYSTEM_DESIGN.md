# TalaAI System Design & Architecture

**Version:** 2.0  
**Last Updated:** 2025-11-28  
**Author:** Backend Team

---

## Table of Contents

1. [Business Model & Value Proposition](#business-model--value-proposition)
2. [System Architecture](#system-architecture)
3. [Database Schema Design](#database-schema-design)
4. [Service Architecture](#service-architecture)
5. [Data Flow & Workflows](#data-flow--workflows)
6. [Scaling Strategy](#scaling-strategy)
7. [Security & Privacy](#security--privacy)

---

## Business Model & Value Proposition

### Core Value Proposition

**TalaAI** is an AI-powered baby care assistant that provides **personalized, intelligent insights** to parents by:

1. **Aggregating multi-source data** (user input, daycare reports, photos, events)
2. **Analyzing patterns** using AI/ML algorithms
3. **Generating actionable insights** tailored to each child's age and development stage
4. **Facilitating parent-AI conversations** for guidance and support

### Target Users

- **Primary:** Parents with children aged 0-5 years
- **Secondary:** Daycare providers, pediatricians (future)

### Business Model

#### Freemium Model
- **Free Tier:**
  - Basic event tracking
  - Limited AI insights (3/day)
  - 100 photos/month
  - 1 child profile

- **Premium Tier ($9.99/month):**
  - Unlimited AI insights
  - Unlimited photo storage
  - Up to 3 child profiles
  - Advanced analytics
  - Daycare integration
  - Priority support

- **Family Plan ($14.99/month):**
  - All Premium features
  - Up to 5 child profiles
  - Multi-user access
  - Export data

### Key Differentiators

1. **Personalization Engine:** Every user sees different content based on:
   - Child's age and development stage
   - User's interests and interaction patterns
   - Recent events and concerns
   - Seasonal and contextual factors

2. **AI-First Approach:** Not just tracking, but intelligent analysis and recommendations

3. **Multi-Source Integration:** Combines user input, daycare data, photos, and external data (weather, local activities)

---

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        Mobile App (iOS)                         │
│                    (Capacitor + React)                          │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTPS/REST
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│                      API Gateway (8080)                         │
│              Authentication, Rate Limiting, Routing             │
└─┬───────┬────────┬────────┬────────┬────────┬────────┬─────────┘
  │       │        │        │        │        │        │
  ▼       ▼        ▼        ▼        ▼        ▼        ▼
┌───┐   ┌───┐   ┌───┐   ┌───┐   ┌───┐   ┌───┐   ┌───┐
│User│   │Event│  │Query│  │ AI │  │Rem│  │Media│  │Pers│
│Svc │   │ Svc │  │ Svc │  │Svc │  │Svc│  │ Svc │  │Svc │
│8084│   │8081 │  │8082 │  │8083│  │8085│  │8086 │  │8087│
└─┬─┘   └─┬─┘   └─┬─┘   └─┬─┘   └─┬─┘   └─┬─┘   └─┬─┘
  │       │       │       │       │       │       │
  └───────┴───────┴───────┴───────┴───────┴───────┘
                          │
        ┌─────────────────┼─────────────────┐
        │                 │                 │
        ▼                 ▼                 ▼
  ┌──────────┐      ┌──────────┐     ┌──────────┐
  │PostgreSQL│      │ClickHouse│     │  Redis   │
  │  (5432)  │      │  (8123)  │     │  (6379)  │
  │Operational│      │Analytics │     │  Cache   │
  └──────────┘      └──────────┘     └──────────┘
        │
        ▼
  ┌──────────┐
  │  Kafka   │
  │  (9092)  │
  │Event Bus │
  └──────────┘
```

### Technology Stack

**Backend:**
- Java 21 (Temurin ARM64)
- Spring Boot 3.2.x
- Spring Cloud Gateway
- PostgreSQL 15 (Operational DB)
- ClickHouse (Analytics DB)
- Redis (Cache)
- Kafka (Event Streaming)

**Frontend:**
- React + TypeScript
- Capacitor (Hybrid App)
- TailwindCSS + shadcn/ui

**AI/ML:**
- Google Gemini API (LLM)
- Mem0 (Memory Management)
- MLX (Audio Processing)

**Infrastructure:**
- Docker + Docker Compose
- Mac Mini (Production)
- GitHub Actions (CI/CD)

---

## Database Schema Design

### Schema Organization

TalaAI uses **schema-per-service** pattern in PostgreSQL:

- `users` - User service tables
- `events` - Event service tables
- `reminders` - Reminder service tables
- `media` - Media service tables
- `files` - File service tables
- `analytics` - Query service tables
- `ai` - AI service tables

### Core Entities

#### 1. User Service Schema (`users`)

##### users
```sql
CREATE TABLE users.users (
    id BIGINT PRIMARY KEY,                    -- Snowflake ID
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_users_email ON users.users(email);
```

##### profiles (Baby Profiles)
```sql
CREATE TABLE users.profiles (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users.users(id),
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
```

##### care_providers (Daycare/Preschool)
```sql
CREATE TABLE users.care_providers (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,              -- DAYCARE, PRESCHOOL, HOME
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_care_providers_type ON users.care_providers(type);
```

##### profile_care_provider_links
```sql
CREATE TABLE users.profile_care_provider_links (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES users.profiles(id),
    care_provider_id BIGINT NOT NULL REFERENCES users.care_providers(id),
    role VARCHAR(20) DEFAULT 'PRIMARY',     -- PRIMARY, SECONDARY
    status VARCHAR(20) DEFAULT 'ACTIVE',    -- ACTIVE, INACTIVE
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_profile_provider UNIQUE (profile_id, care_provider_id)
);

CREATE INDEX idx_pcpl_profile ON users.profile_care_provider_links(profile_id);
CREATE INDEX idx_pcpl_provider ON users.profile_care_provider_links(care_provider_id);
```

##### user_interest_profiles (Personalization)
```sql
CREATE TABLE users.user_interest_profiles (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT NOT NULL,
    interest_vector JSONB,                  -- {"sleep": 0.8, "food": 0.7, ...}
    explicit_topics JSONB,                  -- ["sleep", "development"]
    recent_topics JSONB,                    -- ["food", "health", ...]
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_user_profile UNIQUE (user_id, profile_id)
);

CREATE INDEX idx_uip_user_profile ON users.user_interest_profiles(user_id, profile_id);
```

##### checkin_questions (Daily Checkin)
```sql
CREATE TABLE users.checkin_questions (
    id BIGINT PRIMARY KEY,
    age_min_months INTEGER,
    age_max_months INTEGER,
    topic VARCHAR(50),                      -- sleep, food, health, etc.
    question_text TEXT NOT NULL,
    answer_type VARCHAR(20),                -- scale, boolean, text, choice
    choices JSONB,                          -- ["Great", "Good", "Okay"]
    frequency_hint VARCHAR(20),             -- daily, weekly, monthly
    active BOOLEAN DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_cq_age_range ON users.checkin_questions(age_min_months, age_max_months);
CREATE INDEX idx_cq_topic ON users.checkin_questions(topic);
```

#### 2. Event Service Schema (`events`)

##### events (Universal Event Table)
```sql
CREATE TABLE events.events (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,        -- FEEDING, SLEEP, DIAPER, etc.
    event_time TIMESTAMP NOT NULL,
    event_data JSONB NOT NULL,              -- Flexible event-specific data
    ai_summary TEXT,                        -- AI-generated summary
    ai_tags JSONB,                          -- ["normal", "concern"]
    source VARCHAR(50) DEFAULT 'USER_INPUT', -- USER_INPUT, DAYCARE, API
    priority VARCHAR(20),                   -- low, medium, high, critical
    urgency_hours INTEGER,                  -- Hours until action needed
    risk_level VARCHAR(20),                 -- LOW, MEDIUM, HIGH
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_events_profile_time ON events.events(profile_id, event_time);
CREATE INDEX idx_events_type ON events.events(event_type);
CREATE INDEX idx_events_user ON events.events(user_id);
CREATE INDEX idx_events_deleted ON events.events(deleted_at);
```

**Event Types & Data Structures:**

```javascript
// FEEDING
{
  "type": "BREAST" | "BOTTLE" | "SOLID",
  "amount": 120,
  "unit": "ml",
  "duration_minutes": 15,
  "notes": "Finished everything"
}

// SLEEP
{
  "start_time": "2024-01-01T14:00:00Z",
  "end_time": "2024-01-01T16:00:00Z",
  "duration_minutes": 120,
  "quality": "good" | "fair" | "poor",
  "location": "crib" | "stroller" | "car"
}

// DIAPER
{
  "type": "WET" | "DIRTY" | "BOTH",
  "rash": false,
  "notes": ""
}

// INCIDENT
{
  "incident_type": "FALL" | "INJURY" | "ALLERGY" | "OTHER",
  "severity": "MINOR" | "MODERATE" | "SEVERE",
  "description": "Fell from chair",
  "action_taken": "Applied ice, monitored"
}

// SICKNESS
{
  "symptoms": ["fever", "cough"],
  "temperature": 38.5,
  "temperature_unit": "C",
  "medication_given": "Tylenol",
  "notes": "Consulted pediatrician"
}
```

#### 3. Reminder Service Schema (`reminders`)

##### reminders
```sql
CREATE TABLE reminders.reminders (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT,
    source_event_id BIGINT,                 -- Link to triggering event
    category VARCHAR(50) NOT NULL,          -- prepare, info, appointment, etc.
    title VARCHAR(255) NOT NULL,
    description TEXT,
    due_at TIMESTAMP NOT NULL,
    valid_until TIMESTAMP,
    status VARCHAR(20) DEFAULT 'ACTIVE',    -- ACTIVE, SNOOZED, COMPLETED, CANCELED
    snooze_until TIMESTAMP,
    recurrence_rule VARCHAR(255),           -- RRULE format
    priority VARCHAR(20) DEFAULT 'medium',  -- low, medium, high
    completed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_reminders_user_profile ON reminders.reminders(user_id, profile_id);
CREATE INDEX idx_reminders_due ON reminders.reminders(due_at);
CREATE INDEX idx_reminders_status ON reminders.reminders(status);
CREATE INDEX idx_reminders_category ON reminders.reminders(category);
```

**Reminder Categories:**
- `prepare` - Need to prepare something (pack bag, bring clothes)
- `info` - Information to remember (teacher's note)
- `appointment` - Medical/daycare appointments
- `weather` - Weather-related reminders
- `vaccination` - Vaccination schedule

#### 4. Media Service Schema (`media`)

##### media_items
```sql
CREATE TABLE media.media_items (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    user_id BIGINT,
    care_provider_id BIGINT,
    source VARCHAR(50) DEFAULT 'USER_UPLOADED', -- USER_UPLOADED, DAYCARE_EMAIL, DAYCARE_API
    media_type VARCHAR(20) NOT NULL,        -- PHOTO, VIDEO
    storage_url TEXT NOT NULL,
    thumbnail_url TEXT,
    occurred_at TIMESTAMP,                  -- When photo/video was taken
    ai_tags JSONB,                          -- ["smiling", "outdoor", "eating"]
    faces_count INTEGER,
    emotion_score JSONB,                    -- {"happy": 0.9, "sad": 0.1}
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_media_profile_occurred ON media.media_items(profile_id, occurred_at);
CREATE INDEX idx_media_type ON media.media_items(media_type);
CREATE INDEX idx_media_source ON media.media_items(source);
```

#### 5. File Service Schema (`files`)

##### file_metadata
```sql
CREATE TABLE files.file_metadata (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) UNIQUE NOT NULL,
    file_type VARCHAR(50),                  -- image, video, document
    mime_type VARCHAR(100),
    file_size BIGINT,
    storage_path TEXT,
    public_url TEXT,
    thumbnail_url TEXT,
    width INTEGER,
    height INTEGER,
    duration_seconds INTEGER,
    checksum VARCHAR(64),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_files_user ON files.file_metadata(user_id);
CREATE INDEX idx_files_profile ON files.file_metadata(profile_id);
CREATE INDEX idx_files_type ON files.file_metadata(file_type);
CREATE INDEX idx_files_storage_key ON files.file_metadata(storage_key);
```

#### 6. Analytics Service Schema (`analytics`)

##### daily_child_summaries
```sql
CREATE TABLE analytics.daily_child_summaries (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    date DATE NOT NULL,
    events_summary JSONB,                   -- Event counts by type
    metrics JSONB,                          -- Calculated metrics
    candidate_media_ids JSONB,              -- Top photos for the day
    candidate_incident_ids JSONB,           -- Incidents to highlight
    total_events INTEGER,
    has_incident BOOLEAN,
    has_sickness BOOLEAN,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_profile_date UNIQUE (profile_id, date)
);

CREATE INDEX idx_dcs_profile_date ON analytics.daily_child_summaries(profile_id, date);
```

**Metrics Structure:**
```javascript
{
  "sleep_total_minutes": 720,
  "sleep_sessions": 3,
  "feeding_count": 6,
  "diaper_count": 8,
  "avg_sleep_quality": 4.2,
  "mood_score": 8.5
}
```

#### 7. AI Service Schema (`ai`)

##### ai_today_overviews
```sql
CREATE TABLE ai.ai_today_overviews (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    date DATE NOT NULL,
    summary_sentence TEXT,                  -- "Baby is doing well today..."
    action_suggestion TEXT,                 -- "Monitor baby's condition..."
    pill_topics JSONB,                      -- AI-generated pill topics
    generated_at TIMESTAMP,
    model_version VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_profile_date UNIQUE (profile_id, date)
);

CREATE INDEX idx_ato_profile_date ON ai.ai_today_overviews(profile_id, date);
```

### Database Relationships

```
users.users (1) ──────< (N) users.profiles
users.profiles (1) ────< (N) events.events
users.profiles (1) ────< (N) media.media_items
users.profiles (1) ────< (N) reminders.reminders
users.profiles (N) ────< (N) users.care_providers (via profile_care_provider_links)
users.users (1) ───────< (N) users.user_interest_profiles
events.events (1) ─────< (1) reminders.reminders (source_event_id)
```

### ClickHouse Analytics Schema

```sql
-- Event analytics (denormalized for fast queries)
CREATE TABLE analytics.events_analytics (
    event_id UInt64,
    profile_id UInt64,
    user_id UInt64,
    event_type String,
    event_time DateTime,
    event_date Date,
    event_hour UInt8,
    event_day_of_week UInt8,
    priority String,
    urgency_hours UInt32,
    risk_level String,
    event_data String,                      -- JSON string
    created_at DateTime
) ENGINE = MergeTree()
PARTITION BY toYYYYMM(event_date)
ORDER BY (profile_id, event_date, event_time);

-- Daily aggregations
CREATE TABLE analytics.daily_metrics (
    profile_id UInt64,
    date Date,
    sleep_total_minutes UInt32,
    sleep_sessions UInt16,
    feeding_count UInt16,
    diaper_count UInt16,
    incident_count UInt16,
    avg_mood_score Float32
) ENGINE = SummingMergeTree()
PARTITION BY toYYYYMM(date)
ORDER BY (profile_id, date);
```

---

## Service Architecture

### 1. Gateway Service (Port 8080)

**Responsibilities:**
- API routing
- Authentication (JWT)
- Rate limiting
- Request/response logging
- CORS handling

**Technology:**
- Spring Cloud Gateway
- Spring Security
- Redis (rate limiting)

### 2. User Service (Port 8084)

**Responsibilities:**
- User registration/login
- Profile (baby) management
- Care provider management
- Interest tracking
- Checkin questions

**Key APIs:**
```
POST   /api/v1/auth/register
POST   /api/v1/auth/login
GET    /api/v1/profiles/{id}
POST   /api/v1/profiles
GET    /api/v1/users/interest/scores
POST   /api/v1/users/interest/track
```

### 3. Event Service (Port 8081)

**Responsibilities:**
- Event CRUD operations
- Event validation
- Timeline generation
- Calendar view
- Kafka event publishing

**Key APIs:**
```
POST   /api/v1/events
GET    /api/v1/events/{id}
GET    /api/v1/events/timeline
GET    /api/v1/events/calendar
```

### 4. Query Service (Port 8082)

**Responsibilities:**
- Daily aggregations
- Analytics queries
- Pattern detection
- Trend analysis
- ClickHouse integration

**Key APIs:**
```
GET    /api/v1/analytics/daily-context
GET    /api/v1/analytics/recent-summaries
GET    /api/v1/analytics/patterns
```

### 5. AI Service (Port 8083)

**Responsibilities:**
- AI content generation
- Pattern detection
- Insight generation
- Gemini API integration
- Mem0 memory management

**Key APIs:**
```
GET    /api/v1/ai/patterns/sleep
GET    /api/v1/ai/patterns/feeding
GET    /api/v1/ai/insights
POST   /api/v1/ai/chat
```

### 6. Reminder Service (Port 8085)

**Responsibilities:**
- Reminder CRUD
- Reminder scheduling
- Snooze/complete operations
- Recurrence handling

**Key APIs:**
```
POST   /api/v1/reminders
GET    /api/v1/reminders/due
POST   /api/v1/reminders/{id}/snooze
POST   /api/v1/reminders/{id}/complete
```

### 7. Media Service (Port 8086)

**Responsibilities:**
- Media upload/download
- AI tagging
- Emotion detection
- Thumbnail generation
- MinIO integration

**Key APIs:**
```
POST   /api/v1/media/upload
GET    /api/v1/media/{id}
GET    /api/v1/media?profileId=123&date=2024-01-01
```

### 8. Personalization Service (Port 8087)

**Responsibilities:**
- Today page orchestration
- Insights page generation
- Tala starters generation
- Multi-service aggregation
- Priority/urgency calculation

**Key APIs:**
```
GET    /api/v1/personalization/today
GET    /api/v1/personalization/insights
GET    /api/v1/personalization/tala-starters
```

---

## Data Flow & Workflows

### Workflow 1: User Creates Event

```
1. User Input (Mobile App)
   ↓
2. POST /api/v1/events
   ↓
3. Gateway → Event Service
   ↓
4. Event Service:
   - Validates data
   - Saves to PostgreSQL
   - Publishes to Kafka
   ↓
5. Kafka Consumers:
   - Query Service → Updates daily summary
   - AI Service → Analyzes for patterns
   - Reminder Service → Creates reminders if needed
   ↓
6. Response to User
```

### Workflow 2: Generate Today Page

```
1. User Opens App
   ↓
2. GET /api/v1/personalization/today
   ↓
3. Personalization Service (Orchestrator):
   ├→ User Service: Get profile + interest scores
   ├→ Query Service: Get daily context
   ├→ Event Service: Get recent events
   ├→ Reminder Service: Get active reminders
   ├→ Media Service: Get today's photos
   └→ AI Service: Get AI-generated content
   ↓
4. Personalization Service:
   - Calculates priority scores
   - Calculates urgency scores
   - Selects top topics
   - Builds response
   ↓
5. Cache in Redis (5 minutes)
   ↓
6. Return to User
```

### Workflow 3: AI Insight Generation

```
1. Scheduled Job (Daily 6 AM)
   ↓
2. Query Service:
   - Aggregates yesterday's events
   - Calculates metrics
   - Detects anomalies
   ↓
3. AI Service:
   - Fetches aggregated data
   - Calls Gemini API
   - Generates insights
   - Stores in ai_today_overviews
   ↓
4. Available for Today Page
```

### Workflow 4: Interest Tracking

```
1. User Interacts with Content
   ↓
2. POST /api/v1/users/interest/track
   {topic: "sleep", weight: 1.0}
   ↓
3. User Service:
   - Applies decay to all scores
   - Boosts interacted topic
   - Updates recent topics list
   - Saves to user_interest_profiles
   ↓
4. Next Today Page uses updated scores
```

---

## Scaling Strategy

### Horizontal Scaling

**Stateless Services:**
- All services are stateless
- Can scale horizontally with load balancer
- Session data in Redis

**Database Scaling:**
- PostgreSQL: Read replicas for queries
- ClickHouse: Distributed tables
- Redis: Redis Cluster

### Caching Strategy

**Multi-Level Cache:**

1. **L1: Application Cache** (Caffeine)
   - Today page: 5 minutes
   - Insights: 1 hour
   - Tala starters: 1 hour

2. **L2: Redis Cache**
   - User sessions: 24 hours
   - API responses: 5-60 minutes
   - Rate limiting: 1 minute

3. **L3: CDN** (Future)
   - Static assets
   - Media thumbnails

### Performance Targets

- **API Response Time:** < 200ms (p95)
- **Today Page Generation:** < 500ms
- **Event Creation:** < 100ms
- **Database Query:** < 50ms

---

## Security & Privacy

### Authentication & Authorization

**JWT-based Authentication:**
- Access token: 1 hour expiry
- Refresh token: 7 days expiry
- Stored in httpOnly cookies

**Role-Based Access Control:**
- USER: Standard user
- PREMIUM: Premium subscriber
- ADMIN: System administrator

### Data Privacy

**GDPR Compliance:**
- User data export
- Right to be forgotten (soft delete)
- Data retention policies

**Encryption:**
- Data at rest: PostgreSQL encryption
- Data in transit: TLS 1.3
- Sensitive fields: AES-256

### Security Best Practices

1. **Input Validation:** All inputs validated
2. **SQL Injection Prevention:** Parameterized queries
3. **XSS Prevention:** Output encoding
4. **CSRF Protection:** CSRF tokens
5. **Rate Limiting:** Per-user, per-IP
6. **Audit Logging:** All critical operations logged

---

## Deployment Architecture

### Development Environment

```
MacBook Air (M2)
├── Docker Compose
├── PostgreSQL (local)
├── Redis (local)
└── Services (Maven)
```

### Production Environment

```
Mac Mini (M2 Pro)
├── Docker Swarm / Kubernetes
├── PostgreSQL (persistent volume)
├── ClickHouse (persistent volume)
├── Redis Cluster
├── Kafka Cluster
└── Services (Docker containers)
```

### CI/CD Pipeline

```
GitHub → Actions → Build → Test → Deploy → Monitor
```

---

## Future Enhancements

1. **Real-time Features:**
   - WebSocket for live updates
   - Push notifications

2. **Advanced Analytics:**
   - Predictive models
   - Anomaly detection
   - Growth tracking

3. **Integrations:**
   - Daycare API integrations
   - Pediatrician portals
   - Smart devices (baby monitors)

4. **Multi-language Support:**
   - i18n framework
   - Localized content

5. **Social Features:**
   - Parent community
   - Expert Q&A
   - Content sharing

---

**End of System Design Document**
