ALTER TABLE route_seo_content
    ADD COLUMN IF NOT EXISTS display_from_name VARCHAR(160),
    ADD COLUMN IF NOT EXISTS display_to_name VARCHAR(160);
