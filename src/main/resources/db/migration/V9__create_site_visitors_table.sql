-- Create site_visitors table
CREATE TABLE site_visitors (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ip_address VARCHAR(45) NOT NULL UNIQUE,
    first_visit DATETIME NOT NULL,
    last_visit DATETIME NOT NULL,
    visit_count INT NOT NULL DEFAULT 1,
    user_agent TEXT,
    country VARCHAR(100),
    city VARCHAR(100),
    region VARCHAR(100),
    timezone VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_site_visitors_ip_address ON site_visitors(ip_address);
CREATE INDEX idx_site_visitors_last_visit ON site_visitors(last_visit);
CREATE INDEX idx_site_visitors_visit_count ON site_visitors(visit_count);
CREATE INDEX idx_site_visitors_is_active ON site_visitors(is_active);
CREATE INDEX idx_site_visitors_country ON site_visitors(country);
