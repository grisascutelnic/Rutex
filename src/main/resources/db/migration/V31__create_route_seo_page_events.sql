CREATE TABLE IF NOT EXISTS route_seo_page_events (
    id BIGINT NOT NULL AUTO_INCREMENT,
    route_slug VARCHAR(180) NOT NULL,
    language VARCHAR(2) NOT NULL,
    event_type VARCHAR(40) NOT NULL,
    visitor_key VARCHAR(80) NOT NULL,
    user_id BIGINT,
    ride_id BIGINT,
    page_url VARCHAR(500),
    referrer VARCHAR(500),
    user_agent VARCHAR(500),
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_route_seo_events_route (route_slug, language),
    INDEX idx_route_seo_events_type_created (event_type, created_at),
    INDEX idx_route_seo_events_visitor (visitor_key),
    INDEX idx_route_seo_events_created (created_at)
);
