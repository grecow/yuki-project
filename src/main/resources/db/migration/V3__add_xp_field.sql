-- V3: Add XP field for gamification
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS xp INT DEFAULT 0;
