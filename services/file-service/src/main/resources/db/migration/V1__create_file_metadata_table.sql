-- File Service Migration V1
-- Create file_metadata table in files schema

CREATE TABLE IF NOT EXISTS files.file_metadata (
    id BIGINT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    profile_id BIGINT,
    original_filename VARCHAR(255) NOT NULL,
    storage_key VARCHAR(255) UNIQUE NOT NULL,
    file_type VARCHAR(50),
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

-- Create indexes
CREATE INDEX idx_files_user ON files.file_metadata(user_id);
CREATE INDEX idx_files_profile ON files.file_metadata(profile_id);
CREATE INDEX idx_files_type ON files.file_metadata(file_type);
CREATE INDEX idx_files_storage_key ON files.file_metadata(storage_key);
CREATE INDEX idx_files_checksum ON files.file_metadata(checksum);
CREATE INDEX idx_files_deleted ON files.file_metadata(deleted_at);

-- Add comments
COMMENT ON TABLE files.file_metadata IS 'File storage metadata for MinIO/S3';
COMMENT ON COLUMN files.file_metadata.file_type IS 'image, video, document';
COMMENT ON COLUMN files.file_metadata.storage_key IS 'Unique key in object storage';
COMMENT ON COLUMN files.file_metadata.checksum IS 'SHA-256 checksum for deduplication';
