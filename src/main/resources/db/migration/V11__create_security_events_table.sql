-- Create security_events table
CREATE TABLE security_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    description TEXT NOT NULL,
    user_agent TEXT,
    request_url VARCHAR(500),
    request_method VARCHAR(10),
    timestamp DATETIME NOT NULL,
    is_resolved BOOLEAN DEFAULT FALSE,
    severity VARCHAR(20),
    request_count INT,
    country VARCHAR(100),
    city VARCHAR(100),
    INDEX idx_security_events_ip (ip_address),
    INDEX idx_security_events_type (event_type),
    INDEX idx_security_events_timestamp (timestamp),
    INDEX idx_security_events_severity (severity),
    INDEX idx_security_events_resolved (is_resolved)
);
