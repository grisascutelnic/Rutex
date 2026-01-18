-- Add rating statistics columns to users table if they don't exist
-- This is a safe migration that won't fail if columns already exist

-- Add average_rating column if it doesn't exist
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'average_rating') = 0,
    'ALTER TABLE users ADD COLUMN average_rating DECIMAL(3,2) DEFAULT 0.00',
    'SELECT "average_rating column already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- Add total_ratings column if it doesn't exist
SET @sql = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS 
     WHERE TABLE_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'users' 
     AND COLUMN_NAME = 'total_ratings') = 0,
    'ALTER TABLE users ADD COLUMN total_ratings INT DEFAULT 0',
    'SELECT "total_ratings column already exists" as message'
));
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
