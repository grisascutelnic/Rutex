-- Create table to persist contact action events for ride details
CREATE TABLE IF NOT EXISTS contact_action_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ride_id BIGINT NOT NULL,
    action_type VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_contact_action_type_created (action_type, created_at),
    INDEX idx_contact_action_ride (ride_id)
);
