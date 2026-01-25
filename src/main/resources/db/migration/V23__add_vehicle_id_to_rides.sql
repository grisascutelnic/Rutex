ALTER TABLE rides
    ADD COLUMN vehicle_id BIGINT NULL;

ALTER TABLE rides
    ADD CONSTRAINT fk_rides_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id);

CREATE INDEX idx_rides_vehicle_id ON rides(vehicle_id);
