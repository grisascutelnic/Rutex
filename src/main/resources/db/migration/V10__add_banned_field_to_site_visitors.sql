-- Add banned field to site_visitors table
ALTER TABLE site_visitors ADD COLUMN banned BOOLEAN DEFAULT FALSE;

-- Add index for better performance when checking banned IPs
CREATE INDEX idx_site_visitors_banned ON site_visitors(banned);
CREATE INDEX idx_site_visitors_ip_banned ON site_visitors(ip_address, banned);
