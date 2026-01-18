-- Add country fields to localities table
ALTER TABLE localities 
ADD COLUMN country_code VARCHAR(3),
ADD COLUMN country_name_ro VARCHAR(255),
ADD COLUMN country_name_ru VARCHAR(255);

-- Update existing localities to have Moldova as country
UPDATE localities 
SET country_code = 'MD', 
    country_name_ro = 'Moldova', 
    country_name_ru = 'Молдова'
WHERE country_code IS NULL;

-- Add index for country code for better performance
CREATE INDEX idx_localities_country_code ON localities(country_code);
