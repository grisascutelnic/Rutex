-- Update existing rides to ensure package fields are set correctly
-- Set transport_and_packages to FALSE for all existing rides (default behavior)
UPDATE rides SET transport_and_packages = FALSE WHERE transport_and_packages IS NULL;

-- Set is_package_only to FALSE for all existing rides (default behavior)
UPDATE rides SET is_package_only = FALSE WHERE is_package_only IS NULL;

-- Log the update
SELECT 'Updated ' || COUNT(*) || ' rides with default package field values' as update_message 
FROM rides WHERE transport_and_packages = FALSE AND is_package_only = FALSE;
