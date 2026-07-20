package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.RideView;
import com.scutelnic.rutex.entity.RideViewIP;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.repository.RideViewRepository;
import com.scutelnic.rutex.repository.RideViewIPRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class RideViewService {
    
    @Autowired
    private RideViewRepository rideViewRepository;
    
    @Autowired
    private RideViewIPRepository rideViewIPRepository;
    
    @Autowired
    private RideRepository rideRepository;
    
    /**
     * Înregistrează o vizualizare pentru o cursă dacă IP-ul nu a vizualizat-o deja
     */
    public void recordView(Long rideId, String ipAddress) {
        try {
            // Recording view for ride
            
            // Verificăm dacă cursa există
            Optional<Ride> rideOpt = rideRepository.findById(rideId);
            if (rideOpt.isEmpty()) {
                System.out.println("❌ Ride not found with ID: " + rideId);
                return;
            }
            
            Ride ride = rideOpt.get();
            
            // Verificăm dacă IP-ul a vizualizat deja această cursă
            Optional<RideViewIP> existingViewIP = rideViewIPRepository.findByRideIdAndIpAddress(rideId, ipAddress);
            if (existingViewIP.isPresent()) {
                // IP already viewed ride
                return;
            }
            
            // Înregistrăm IP-ul care a vizualizat
            RideViewIP rideViewIP = new RideViewIP();
            rideViewIP.setRide(ride);
            rideViewIP.setIpAddress(ipAddress);
            rideViewIPRepository.save(rideViewIP);
            System.out.println("✅ Recorded IP view for ride " + rideId);
            
            // Actualizăm sau creăm înregistrarea de vizualizări
            Optional<RideView> rideViewOpt = rideViewRepository.findByRideId(rideId);
            RideView rideView;
            
            if (rideViewOpt.isPresent()) {
                // Actualizăm înregistrarea existentă
                rideView = rideViewOpt.get();
                rideView.setViewCount(rideView.getViewCount() + 1);
                System.out.println("📈 Updated view count for ride " + rideId + " to " + rideView.getViewCount());
            } else {
                // Creăm o nouă înregistrare
                rideView = new RideView();
                rideView.setRide(ride);
                rideView.setViewCount(1L);
                System.out.println("🆕 Created new view record for ride " + rideId + " with count 1");
            }
            
            rideViewRepository.save(rideView);
            System.out.println("✅ View recorded successfully for ride " + rideId);
            
        } catch (Exception e) {
            System.err.println("❌ Error recording view for ride " + rideId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Obține numărul de vizualizări pentru o cursă
     */
    public Long getViewCount(Long rideId) {
        try {
            Optional<RideView> rideViewOpt = rideViewRepository.findByRideId(rideId);
            return rideViewOpt.map(RideView::getViewCount).orElse(0L);
        } catch (Exception e) {
            System.err.println("❌ Error getting view count for ride " + rideId + ": " + e.getMessage());
            return 0L;
        }
    }
    
    /**
     * Șterge toate vizualizările pentru o cursă (când se șterge cursa)
     */
    public void deleteViewsForRide(Long rideId) {
        rideViewIPRepository.deleteByRideId(rideId);
        rideViewRepository.deleteByRideId(rideId);
    }
    
    /**
     * Verifică dacă un IP a vizualizat deja o cursă
     */
    public boolean hasIPViewedRide(Long rideId, String ipAddress) {
        try {
            Optional<RideViewIP> existingViewIP = rideViewIPRepository.findByRideIdAndIpAddress(rideId, ipAddress);
            return existingViewIP.isPresent();
        } catch (Exception e) {
            System.err.println("❌ Error checking if IP viewed ride: " + e.getMessage());
            return false;
        }
    }
}
