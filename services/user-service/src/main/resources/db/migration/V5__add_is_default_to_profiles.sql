-- Add is_default column to profiles table
ALTER TABLE users.profiles
ADD COLUMN is_default BOOLEAN DEFAULT FALSE;

-- Set the first profile for each user as default
WITH first_profiles AS (
    SELECT DISTINCT ON (user_id) id, user_id
    FROM users.profiles
    WHERE deleted_at IS NULL
    ORDER BY user_id, created_at ASC
)
UPDATE users.profiles
SET is_default = TRUE
WHERE id IN (SELECT id FROM first_profiles);

-- Add index for faster queries on default profiles
CREATE INDEX idx_profiles_user_id_is_default ON users.profiles(user_id, is_default) WHERE deleted_at IS NULL;
