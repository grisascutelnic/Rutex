-- Force update all existing rides to ensure package fields are set correctly
-- This migration ensures that all rides have explicit values for package fields

-- Update all rides to set is_package_only to FALSE (default for existing rides)
UPDATE rides SET is_package_only = FALSE;

-- Update all rides to set transport_and_packages to FALSE (default for existing rides)
UPDATE rides SET transport_and_packages = FALSE;

-- Log the update
SELECT 'Force updated ' || COUNT(*) || ' rides with package field values' as update_message 
FROM rides;
