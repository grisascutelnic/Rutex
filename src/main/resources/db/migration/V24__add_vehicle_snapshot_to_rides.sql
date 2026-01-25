ALTER TABLE rides
    ADD COLUMN vehicle_make VARCHAR(100) NULL,
    ADD COLUMN vehicle_color VARCHAR(50) NULL,
    ADD COLUMN vehicle_plate_number VARCHAR(50) NULL;

ALTER TABLE rides
    DROP FOREIGN KEY fk_rides_vehicle;

ALTER TABLE rides
    ADD CONSTRAINT fk_rides_vehicle
        FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL;
