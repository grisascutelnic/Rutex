-- Add transport_and_packages column to rides table
ALTER TABLE rides ADD COLUMN transport_and_packages BOOLEAN NOT NULL DEFAULT FALSE;
