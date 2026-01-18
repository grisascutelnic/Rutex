-- Update existing localities to have Moldova as country if no country code is set
UPDATE localities 
SET country_code = 'MD', 
    country_name_ro = 'Moldova', 
    country_name_ru = 'Молдова'
WHERE country_code IS NULL OR country_code = '';

-- Update localities that are clearly Moldovan (have Romanian/Russian names and are in known districts)
UPDATE localities 
SET country_code = 'MD', 
    country_name_ro = 'Moldova', 
    country_name_ru = 'Молдова'
WHERE (name_ro LIKE '%ă%' OR name_ro LIKE '%ș%' OR name_ro LIKE '%ț%' OR name_ru LIKE '%а%' OR name_ru LIKE '%е%' OR name_ru LIKE '%и%')
AND (country_code IS NULL OR country_code = '' OR country_code != 'MD');
