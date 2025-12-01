-- V2: Add attachment_file_ids to original_events and remove photo/attachment URL columns

ALTER TABLE origin_data.original_events
    ADD COLUMN attachment_file_ids JSONB NOT NULL DEFAULT '[]';

ALTER TABLE origin_data.daycare_report_items
    DROP COLUMN photo_urls;

ALTER TABLE origin_data.home_events
    DROP COLUMN photo_urls;

ALTER TABLE origin_data.incident_reports
    DROP COLUMN photo_urls;

ALTER TABLE origin_data.health_reports
    DROP COLUMN photo_urls;

ALTER TABLE origin_data.timeline_entries
    DROP COLUMN attachment_urls;
