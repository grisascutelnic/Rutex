-- Clean up duplicate ratings before enforcing unique constraint
-- This migration removes duplicate ratings keeping only the most recent one

-- First, let's see if there are any duplicates
SELECT 'Checking for duplicate ratings...' as message;

-- Remove duplicate ratings keeping only the most recent one for each rater-rated_user pair
DELETE r1 FROM ratings r1
INNER JOIN ratings r2 
WHERE r1.id < r2.id 
AND r1.rater_id = r2.rater_id 
AND r1.rated_user_id = r2.rated_user_id;

-- Verify no duplicates remain
SELECT 'Duplicate cleanup completed' as message;
