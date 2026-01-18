-- Update existing users to have +373 as default prefix if phonePrefix is null
UPDATE users SET phone_prefix = '+373' WHERE phone_prefix IS NULL;

-- Also update users who have phone numbers starting with 0 to remove the 0
-- This will normalize existing phone numbers to match the new format
UPDATE users 
SET phone = SUBSTRING(phone, 2) 
WHERE phone LIKE '0%' AND LENGTH(phone) > 1;
