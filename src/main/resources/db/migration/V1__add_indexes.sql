CREATE INDEX IF NOT EXISTS idx_site_visitors_active_last_visit
    ON site_visitors (is_active, last_visit);

CREATE INDEX IF NOT EXISTS idx_site_visitors_active_visit_count
    ON site_visitors (is_active, visit_count);

CREATE INDEX IF NOT EXISTS idx_site_visitors_active_first_visit
    ON site_visitors (is_active, first_visit);

CREATE INDEX IF NOT EXISTS idx_site_visitors_country_active
    ON site_visitors (country, is_active);

CREATE INDEX IF NOT EXISTS idx_site_visitors_banned_last_visit
    ON site_visitors (banned, last_visit);

CREATE INDEX IF NOT EXISTS idx_security_events_timestamp
    ON security_events (timestamp);

CREATE INDEX IF NOT EXISTS idx_security_events_ip_timestamp
    ON security_events (ip_address, timestamp);

CREATE INDEX IF NOT EXISTS idx_security_events_event_type_timestamp
    ON security_events (event_type, timestamp);

CREATE INDEX IF NOT EXISTS idx_security_events_severity_timestamp
    ON security_events (severity, timestamp);

CREATE INDEX IF NOT EXISTS idx_security_events_is_resolved_timestamp
    ON security_events (is_resolved, timestamp);
