-- Add last seen timestamp for user presence
ALTER TABLE users
    ADD COLUMN last_seen_at TIMESTAMP NULL;
