-- User Service Migration V3
-- Create care providers and linking tables

-- Care providers table
CREATE TABLE IF NOT EXISTS users.care_providers (
    id BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type VARCHAR(50) NOT NULL,
    email VARCHAR(255),
    phone VARCHAR(50),
    address TEXT,
    timezone VARCHAR(50) DEFAULT 'UTC',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

CREATE INDEX idx_care_providers_type ON users.care_providers(type);
CREATE INDEX idx_care_providers_deleted ON users.care_providers(deleted_at);

COMMENT ON TABLE users.care_providers IS 'Daycare, preschool, and other care providers';
COMMENT ON COLUMN users.care_providers.type IS 'DAYCARE, PRESCHOOL, HOME, NANNY';

-- Profile-CareProvider linking table
CREATE TABLE IF NOT EXISTS users.profile_care_provider_links (
    id BIGINT PRIMARY KEY,
    profile_id BIGINT NOT NULL,
    care_provider_id BIGINT NOT NULL,
    role VARCHAR(20) DEFAULT 'PRIMARY',
    status VARCHAR(20) DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uk_profile_provider UNIQUE (profile_id, care_provider_id)
);

CREATE INDEX idx_pcpl_profile ON users.profile_care_provider_links(profile_id);
CREATE INDEX idx_pcpl_provider ON users.profile_care_provider_links(care_provider_id);
CREATE INDEX idx_pcpl_status ON users.profile_care_provider_links(status);
CREATE INDEX idx_pcpl_deleted ON users.profile_care_provider_links(deleted_at);

COMMENT ON TABLE users.profile_care_provider_links IS 'Links children to their care providers';
COMMENT ON COLUMN users.profile_care_provider_links.role IS 'PRIMARY, SECONDARY';
COMMENT ON COLUMN users.profile_care_provider_links.status IS 'ACTIVE, INACTIVE';
