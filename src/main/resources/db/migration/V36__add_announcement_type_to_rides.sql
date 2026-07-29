ALTER TABLE rides
    ADD COLUMN announcement_type VARCHAR(30) NOT NULL DEFAULT 'DRIVER_OFFER',
    ADD COLUMN requested_seats INT NULL,
    ADD COLUMN flexible_time BOOLEAN NOT NULL DEFAULT FALSE;

CREATE INDEX idx_rides_announcement_active_created
    ON rides (announcement_type, is_active, created_at);
