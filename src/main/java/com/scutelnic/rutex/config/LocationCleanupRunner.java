package com.scutelnic.rutex.config;

import com.scutelnic.rutex.service.RideService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class LocationCleanupRunner implements CommandLineRunner {

    private final RideService rideService;
    private final boolean enabled;

    public LocationCleanupRunner(RideService rideService,
                                 @Value("${app.data.cleanup.locations:false}") boolean enabled) {
        this.rideService = rideService;
        this.enabled = enabled;
    }

    @Override
    public void run(String... args) {
        if (!enabled) {
            return;
        }
        int updated = rideService.cleanupLocationData();
        System.out.println("Location cleanup completed. Updated rides: " + updated);
    }
}
