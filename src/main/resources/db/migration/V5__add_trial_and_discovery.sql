-- V5: Trial system and discovery lesson tracking
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS discovery_done BOOLEAN DEFAULT FALSE;
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS trial_start_date TIMESTAMP NULL;
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS trial_end_date TIMESTAMP NULL;
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS daily_conversations_used INT DEFAULT 0;
ALTER TABLE yuki_data ADD COLUMN IF NOT EXISTS total_conversations INT DEFAULT 0;
