-- Add onboarding fields to profiles table
-- These fields store additional information collected during user onboarding

ALTER TABLE users.profiles
ADD COLUMN parent_name VARCHAR(255),
ADD COLUMN parent_role VARCHAR(50),
ADD COLUMN zipcode VARCHAR(10),
ADD COLUMN concerns TEXT,
ADD COLUMN has_daycare BOOLEAN DEFAULT FALSE,
ADD COLUMN daycare_name VARCHAR(255),
ADD COLUMN update_method VARCHAR(100);

-- Add indexes for commonly queried fields
CREATE INDEX idx_profiles_zipcode ON users.profiles(zipcode) WHERE deleted_at IS NULL;
CREATE INDEX idx_profiles_has_daycare ON users.profiles(has_daycare) WHERE deleted_at IS NULL;

-- Add comments
COMMENT ON COLUMN users.profiles.parent_name IS 'Parent or caregiver first name';
COMMENT ON COLUMN users.profiles.parent_role IS 'Parent role: Mom, Dad, Grandmother, Grandfather, Other caregiver';
COMMENT ON COLUMN users.profiles.zipcode IS 'Zipcode for weather and health alerts';
COMMENT ON COLUMN users.profiles.concerns IS 'Comma-separated list of parent concerns from onboarding';
COMMENT ON COLUMN users.profiles.has_daycare IS 'Whether child attends daycare or preschool';
COMMENT ON COLUMN users.profiles.daycare_name IS 'Name of daycare or preschool';
COMMENT ON COLUMN users.profiles.update_method IS 'How parent receives daycare updates';
