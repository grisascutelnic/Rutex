-- Create statistics table to persist search and API call statistics
CREATE TABLE IF NOT EXISTS statistics (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stat_key VARCHAR(100) NOT NULL UNIQUE,
    stat_value BIGINT NOT NULL DEFAULT 0,
    last_updated TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Insert initial statistics
INSERT INTO statistics (stat_key, stat_value) VALUES
('total_local_searches', 0),
('total_google_api_searches', 0),
('today_local_searches', 0),
('today_google_api_searches', 0),
('week_local_searches', 0),
('week_google_api_searches', 0),
('month_local_searches', 0),
('month_google_api_searches', 0),
('total_google_places_api_calls', 0),
('today_google_places_api_calls', 0),
('week_google_places_api_calls', 0),
('month_google_places_api_calls', 0),
('last_reset_time', UNIX_TIMESTAMP() * 1000)
ON DUPLICATE KEY UPDATE stat_value = VALUES(stat_value);
