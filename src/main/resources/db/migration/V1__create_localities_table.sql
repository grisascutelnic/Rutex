-- Create localities table
CREATE TABLE IF NOT EXISTS localities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_ro VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    google_place_id VARCHAR(255) UNIQUE,
    country_code VARCHAR(3),
    country_name_ro VARCHAR(255),
    country_name_ru VARCHAR(255),
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    type VARCHAR(50),
    district_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    search_count INT DEFAULT 0,
    INDEX idx_google_place_id (google_place_id),
    INDEX idx_name_ro (name_ro),
    INDEX idx_name_ru (name_ru),
    INDEX idx_country_code (country_code),
    INDEX idx_search_count (search_count)
);

-- Create districts table if not exists
CREATE TABLE IF NOT EXISTS districts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_ro VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name_ro (name_ro),
    INDEX idx_name_ru (name_ru)
);
