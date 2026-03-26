-- V2: Add indexes on frequently queried columns and timestamps

-- Indexes
CREATE INDEX IF NOT EXISTS idx_user_uid ON user(uid);
CREATE INDEX IF NOT EXISTS idx_user_email ON user(email);
CREATE INDEX IF NOT EXISTS idx_user_original_app_user_id ON user(original_app_user_id);
CREATE INDEX IF NOT EXISTS idx_lesson_language_published ON lesson(language, published);

-- Add timestamps to user table
ALTER TABLE user ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE user ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- Add timestamps to yuki_data table
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;
