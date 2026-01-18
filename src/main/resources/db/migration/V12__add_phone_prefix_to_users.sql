-- Add phonePrefix column to users table
ALTER TABLE users ADD COLUMN phone_prefix VARCHAR(10);

-- Update existing users to have +373 as default prefix
UPDATE users SET phone_prefix = '+373' WHERE phone_prefix IS NULL;
