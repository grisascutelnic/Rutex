-- Comandă SQL pentru actualizarea utilizatorilor existenți
-- Rulează această comandă în baza de date pentru a actualiza utilizatorii existenți

-- 1. Actualizează utilizatorii care au phonePrefix NULL să aibă +373
UPDATE users SET phone_prefix = '+373' WHERE phone_prefix IS NULL;

-- 2. Elimină 0-ul de la început pentru numerele Moldova (dacă au 9 cifre și încep cu 0)
UPDATE users 
SET phone = SUBSTRING(phone, 2) 
WHERE phone LIKE '0%' AND LENGTH(phone) = 9;

-- 3. Verifică rezultatele
SELECT 
    id,
    email,
    phone,
    phone_prefix,
    CASE 
        WHEN phone_prefix IS NULL THEN 'NEEDS UPDATE'
        ELSE 'OK'
    END as status
FROM users 
ORDER BY id;

-- 4. Numărul de utilizatori actualizați
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN phone_prefix IS NOT NULL THEN 1 END) as users_with_prefix,
    COUNT(CASE WHEN phone_prefix IS NULL THEN 1 END) as users_without_prefix
FROM users;
