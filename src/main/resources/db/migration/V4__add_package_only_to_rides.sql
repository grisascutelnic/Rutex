-- Add isPackageOnly column to rides table
ALTER TABLE rides ADD COLUMN is_package_only BOOLEAN NOT NULL DEFAULT FALSE;
