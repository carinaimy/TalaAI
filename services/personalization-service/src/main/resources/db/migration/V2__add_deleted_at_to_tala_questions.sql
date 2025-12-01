-- Add soft delete column to Tala Questions to match BaseEntity

ALTER TABLE personalization.tala_questions
    ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL;
