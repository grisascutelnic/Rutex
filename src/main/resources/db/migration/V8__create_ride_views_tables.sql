-- Create ride_views table
CREATE TABLE ride_views (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ride_id BIGINT NOT NULL UNIQUE,
    view_count BIGINT NOT NULL DEFAULT 0,
    last_viewed TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE
);

-- Create ride_view_ips table
CREATE TABLE ride_view_ips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ride_id BIGINT NOT NULL,
    ip_address VARCHAR(45) NOT NULL,
    viewed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (ride_id) REFERENCES rides(id) ON DELETE CASCADE,
    UNIQUE KEY unique_ride_ip (ride_id, ip_address)
);

-- Create indexes for better performance
CREATE INDEX idx_ride_views_ride_id ON ride_views(ride_id);
CREATE INDEX idx_ride_view_ips_ride_id ON ride_view_ips(ride_id);
CREATE INDEX idx_ride_view_ips_ip_address ON ride_view_ips(ip_address);
