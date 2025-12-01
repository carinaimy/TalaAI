-- Origin Data Service - Initial Schema
-- Event Sourcing Architecture with Original + AI Data Separation

-- ============================================================================
-- 1. Original Event (Top-level event sourcing table)
-- ============================================================================
CREATE TABLE origin_data.original_events (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    source_event_id VARCHAR(255),
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    raw_payload JSONB NOT NULL,
    ai_processed BOOLEAN DEFAULT FALSE,
    ai_processed_at TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_original_event_profile_id ON origin_data.original_events(profile_id);
CREATE INDEX idx_original_event_source_type ON origin_data.original_events(source_type);
CREATE INDEX idx_original_event_source_id ON origin_data.original_events(source_event_id);
CREATE INDEX idx_original_event_time ON origin_data.original_events(event_time);
CREATE INDEX idx_original_event_ai_processed ON origin_data.original_events(ai_processed);
CREATE UNIQUE INDEX idx_original_event_source_unique ON origin_data.original_events(source_type, source_event_id) WHERE source_event_id IS NOT NULL;

COMMENT ON TABLE origin_data.original_events IS 'Top-level event sourcing table for all external data';
COMMENT ON COLUMN origin_data.original_events.raw_payload IS 'Original JSON payload from external source';
COMMENT ON COLUMN origin_data.original_events.ai_processed IS 'Whether AI has processed this event';

-- ============================================================================
-- 2. Day Care Report (Header + Detail)
-- ============================================================================
CREATE TABLE origin_data.daycare_reports (
    id BIGINT PRIMARY KEY,
    original_event_id BIGINT NOT NULL UNIQUE REFERENCES origin_data.original_events(id),
    profile_id BIGINT NOT NULL,
    report_date DATE NOT NULL,
    daycare_name VARCHAR(255),
    teacher_name VARCHAR(255),
    summary TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_daycare_report_original_event ON origin_data.daycare_reports(original_event_id);
CREATE INDEX idx_daycare_report_profile_date ON origin_data.daycare_reports(profile_id, report_date);

CREATE TABLE origin_data.daycare_report_items (
    id BIGINT PRIMARY KEY,
    daycare_report_id BIGINT NOT NULL REFERENCES origin_data.daycare_reports(id) ON DELETE CASCADE,
    item_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE,
    title VARCHAR(255),
    description TEXT,
    details JSONB,
    photo_urls TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_daycare_item_report ON origin_data.daycare_report_items(daycare_report_id);
CREATE INDEX idx_daycare_item_type ON origin_data.daycare_report_items(item_type);
CREATE INDEX idx_daycare_item_time ON origin_data.daycare_report_items(event_time);

COMMENT ON TABLE origin_data.daycare_reports IS 'Daily reports from daycare facilities';
COMMENT ON TABLE origin_data.daycare_report_items IS 'Individual activities within daycare reports';

-- ============================================================================
-- 3. Home Event
-- ============================================================================
CREATE TABLE origin_data.home_events (
    id BIGINT PRIMARY KEY,
    original_event_id BIGINT NOT NULL UNIQUE REFERENCES origin_data.original_events(id),
    profile_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    event_time TIMESTAMP WITH TIME ZONE NOT NULL,
    title VARCHAR(255),
    description TEXT,
    details JSONB,
    photo_urls TEXT,
    location VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_home_event_original_event ON origin_data.home_events(original_event_id);
CREATE INDEX idx_home_event_profile ON origin_data.home_events(profile_id);
CREATE INDEX idx_home_event_type ON origin_data.home_events(event_type);
CREATE INDEX idx_home_event_time ON origin_data.home_events(event_time);

COMMENT ON TABLE origin_data.home_events IS 'Events recorded by parents at home';

-- ============================================================================
-- 4. Incident Report
-- ============================================================================
CREATE TABLE origin_data.incident_reports (
    id BIGINT PRIMARY KEY,
    original_event_id BIGINT NOT NULL UNIQUE REFERENCES origin_data.original_events(id),
    profile_id BIGINT NOT NULL,
    incident_time TIMESTAMP WITH TIME ZONE NOT NULL,
    title VARCHAR(255),
    story TEXT,
    involved_people TEXT,
    severity VARCHAR(50),
    handling_action TEXT,
    result TEXT,
    location VARCHAR(255),
    photo_urls TEXT,
    reported_by VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_incident_report_original_event ON origin_data.incident_reports(original_event_id);
CREATE INDEX idx_incident_report_profile ON origin_data.incident_reports(profile_id);
CREATE INDEX idx_incident_report_severity ON origin_data.incident_reports(severity);
CREATE INDEX idx_incident_report_time ON origin_data.incident_reports(incident_time);

COMMENT ON TABLE origin_data.incident_reports IS 'Incident reports from daycare or other sources';

-- ============================================================================
-- 5. Health Report (Header + Detail Tables)
-- ============================================================================
CREATE TABLE origin_data.health_reports (
    id BIGINT PRIMARY KEY,
    original_event_id BIGINT NOT NULL UNIQUE REFERENCES origin_data.original_events(id),
    profile_id BIGINT NOT NULL,
    report_type VARCHAR(50) NOT NULL,
    visit_time TIMESTAMP WITH TIME ZONE NOT NULL,
    provider_name VARCHAR(255),
    facility_name VARCHAR(255),
    diagnosis TEXT,
    summary TEXT,
    next_appointment TIMESTAMP WITH TIME ZONE,
    photo_urls TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_health_report_original_event ON origin_data.health_reports(original_event_id);
CREATE INDEX idx_health_report_profile ON origin_data.health_reports(profile_id);
CREATE INDEX idx_health_report_type ON origin_data.health_reports(report_type);
CREATE INDEX idx_health_report_visit_time ON origin_data.health_reports(visit_time);

CREATE TABLE origin_data.health_measurements (
    id BIGINT PRIMARY KEY,
    health_report_id BIGINT NOT NULL REFERENCES origin_data.health_reports(id) ON DELETE CASCADE,
    measurement_type VARCHAR(100) NOT NULL,
    value DECIMAL(10, 2),
    unit VARCHAR(50),
    percentile DECIMAL(5, 2),
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_health_measurement_report ON origin_data.health_measurements(health_report_id);
CREATE INDEX idx_health_measurement_type ON origin_data.health_measurements(measurement_type);

CREATE TABLE origin_data.health_medications (
    id BIGINT PRIMARY KEY,
    health_report_id BIGINT NOT NULL REFERENCES origin_data.health_reports(id) ON DELETE CASCADE,
    medication_name VARCHAR(255) NOT NULL,
    dosage VARCHAR(100),
    frequency VARCHAR(100),
    start_date TIMESTAMP WITH TIME ZONE,
    end_date TIMESTAMP WITH TIME ZONE,
    purpose TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_health_medication_report ON origin_data.health_medications(health_report_id);

CREATE TABLE origin_data.health_vaccinations (
    id BIGINT PRIMARY KEY,
    health_report_id BIGINT NOT NULL REFERENCES origin_data.health_reports(id) ON DELETE CASCADE,
    vaccine_name VARCHAR(255) NOT NULL,
    dose_number INTEGER,
    administered_date TIMESTAMP WITH TIME ZONE,
    lot_number VARCHAR(100),
    next_dose_due TIMESTAMP WITH TIME ZONE,
    reaction TEXT,
    notes TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_health_vaccination_report ON origin_data.health_vaccinations(health_report_id);
CREATE INDEX idx_health_vaccination_name ON origin_data.health_vaccinations(vaccine_name);

COMMENT ON TABLE origin_data.health_reports IS 'Medical visits, checkups, and health records';
COMMENT ON TABLE origin_data.health_measurements IS 'Physical measurements (height, weight, etc.)';
COMMENT ON TABLE origin_data.health_medications IS 'Prescribed medications';
COMMENT ON TABLE origin_data.health_vaccinations IS 'Vaccination records';

-- ============================================================================
-- 6. Timeline Entry (AI-generated display data)
-- ============================================================================
CREATE TABLE origin_data.timeline_entries (
    id BIGINT PRIMARY KEY,
    original_event_id BIGINT NOT NULL REFERENCES origin_data.original_events(id),
    profile_id BIGINT NOT NULL,
    timeline_type VARCHAR(50) NOT NULL,
    data_source VARCHAR(50) NOT NULL,
    record_time TIMESTAMP WITH TIME ZONE NOT NULL,
    title VARCHAR(255),
    ai_summary TEXT,
    ai_tags JSONB,
    attachment_urls TEXT,
    location VARCHAR(255),
    ai_model_version VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    deleted_at TIMESTAMP WITH TIME ZONE
);

CREATE INDEX idx_timeline_entry_profile ON origin_data.timeline_entries(profile_id);
CREATE INDEX idx_timeline_entry_type ON origin_data.timeline_entries(timeline_type);
CREATE INDEX idx_timeline_entry_source ON origin_data.timeline_entries(data_source);
CREATE INDEX idx_timeline_entry_time ON origin_data.timeline_entries(record_time);
CREATE INDEX idx_timeline_entry_original ON origin_data.timeline_entries(original_event_id);
CREATE INDEX idx_timeline_entry_profile_time ON origin_data.timeline_entries(profile_id, record_time DESC);

COMMENT ON TABLE origin_data.timeline_entries IS 'AI-generated timeline entries for display in app';
COMMENT ON COLUMN origin_data.timeline_entries.ai_summary IS 'AI-generated human-readable summary';
COMMENT ON COLUMN origin_data.timeline_entries.ai_tags IS 'AI-extracted tags (emotions, keywords, etc.)';
