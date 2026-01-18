package com.scutelnic.rutex.config;

import com.scutelnic.rutex.service.RideService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Configuration
@EnableScheduling
public class SchedulerConfig {
    
    @Autowired
    private RideService rideService;
    
    // Ruleaza la fiecare 30 de minute pentru a marca calatoriile expirate ca inactive
    @Scheduled(cron = "0 */30 * * * *")
    public void markExpiredRidesAsInactive() {
        try {
            rideService.cleanupExpiredRides();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    // Ruleaza la fiecare 5 minute pentru a marca calatoriile completate ca inactive
    @Scheduled(cron = "0 */5 * * * *")
    public void markCompletedRidesAsInactive() {
        try {
            rideService.markCompletedRidesAsInactive();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
