CREATE TABLE IF NOT EXISTS route_seo_content (
    id BIGINT NOT NULL AUTO_INCREMENT,
    route_slug VARCHAR(180) NOT NULL,
    language VARCHAR(2) NOT NULL,
    from_location VARCHAR(255) NOT NULL,
    to_location VARCHAR(255) NOT NULL,
    route_description VARCHAR(2000),
    from_description VARCHAR(2000),
    to_description VARCHAR(2000),
    nearby_directions_text VARCHAR(2000),
    source VARCHAR(40),
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_route_seo_slug_language UNIQUE (route_slug, language)
);

