package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.service.RideViewService;
import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.dto.SearchRideRequest;
import com.scutelnic.rutex.dto.AddRideRequest;
import com.scutelnic.rutex.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/rides")
public class RideController {
    
    @Autowired
    private RideService rideService;
    
    @Autowired
    private RideViewService rideViewService;
    
    @GetMapping
    public ResponseEntity<List<RideDTO>> getAllRides() {
        try {
            // API call to get all rides
            List<RideDTO> rides = rideService.getAllActiveRides();
            // Returning rides
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            System.err.println("Error in getAllRides: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).build();
        }
    }
    
    
    
    @GetMapping("/{id}")
    public ResponseEntity<RideDTO> getRideById(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Obținem IP-ul clientului
            String clientIP = getClientIPAddress(request);
            
            // Înregistrăm vizualizarea
            rideViewService.recordView(id, clientIP);
            
            RideDTO ride = rideService.getRideById(id);
            return ResponseEntity.ok(ride);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    
    /**
     * Obține IP-ul real al clientului, ținând cont de proxy-uri
     */
    private String getClientIPAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty() && !"unknown".equalsIgnoreCase(xRealIP)) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }
    
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchRides(
            @RequestParam(required = false) String fromLocation,
            @RequestParam(required = false) String toLocation,
            @RequestParam(required = false) String travelDate,
            @RequestParam(defaultValue = "1") int passengers,
            @RequestParam(defaultValue = "0") int luggage,
            @RequestParam(required = false) String packages) {
        
        try {
            SearchRideRequest request = new SearchRideRequest();
            
            // Setăm doar câmpurile care au fost furnizate
            if (fromLocation != null && !fromLocation.trim().isEmpty()) {
                request.setFromLocation(fromLocation.trim());
            }
            
            if (toLocation != null && !toLocation.trim().isEmpty()) {
                request.setToLocation(toLocation.trim());
            }
            
            if (travelDate != null && !travelDate.trim().isEmpty()) {
                request.setTravelDate(LocalDate.parse(travelDate));
            }
            
            request.setPassengers(passengers);
            request.setLuggage(luggage);
            
            // Procesăm parametrul packages pentru a filtra cursele de colete
            if (packages != null && packages.equals("on")) {
                // Setăm passengers = 0 pentru a indica că vrem doar curse de colete
                request.setPassengers(0);
            }
            
            // Logging pentru debugging
            // Search request received
                    // Search parameters processed
            
            List<RideDTO> results = rideService.searchRides(request);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            
            // Construim un mesaj descriptiv în funcție de câmpurile furnizate
            StringBuilder message = new StringBuilder("Căutarea a fost realizată cu succes!");
            if (fromLocation != null && !fromLocation.trim().isEmpty()) {
                message.append(" De la: ").append(fromLocation.trim());
            }
            if (toLocation != null && !toLocation.trim().isEmpty()) {
                message.append(" Până la: ").append(toLocation.trim());
            }
            if (travelDate != null && !travelDate.trim().isEmpty()) {
                message.append(" Data: ").append(travelDate);
            }
            if (fromLocation == null && toLocation == null && travelDate == null) {
                message.append(" (toate cursele disponibile)");
            }
            
            response.put("message", message.toString());
            response.put("results", results);
            response.put("count", results.size());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la căutare: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> addRide(
            @RequestParam String fromLocation,
            @RequestParam String toLocation,
            @RequestParam String travelDate,
            @RequestParam String departureTime,
            @RequestParam(defaultValue = "1") int availableSeats,
            @RequestParam double price,
            @RequestParam String description,
            @RequestParam(defaultValue = "false") boolean isPackageOnly,
            @RequestParam(defaultValue = "false") boolean transportAndPackages,
            HttpSession session) {
        
        try {
            // Verificăm dacă utilizatorul este logat
            User user = (User) session.getAttribute("user");
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a adăuga o cursă.");
                return ResponseEntity.status(401).body(response);
            }
            
            AddRideRequest request = new AddRideRequest();
            request.setFromLocation(fromLocation);
            request.setToLocation(toLocation);
            request.setTravelDate(LocalDate.parse(travelDate));
            request.setDepartureTime(LocalTime.parse(departureTime));
            // Pentru transport de colete, setăm automat availableSeats = 0
            request.setAvailableSeats(isPackageOnly ? 0 : availableSeats);
            request.setPrice(BigDecimal.valueOf(price));
            request.setDescription(description);
            request.setIsPackageOnly(isPackageOnly);
            request.setTransportAndPackages(transportAndPackages);
            
            RideDTO savedRide = rideService.addRide(request, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cursa a fost adăugată cu succes!");
            response.put("ride", savedRide);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la adăugarea cursei: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/locations/from")
    public ResponseEntity<List<String>> getAllFromLocations() {
        List<String> locations = rideService.getAllFromLocations();
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/locations/to")
    public ResponseEntity<List<String>> getAllToLocations() {
        List<String> locations = rideService.getAllToLocations();
        return ResponseEntity.ok(locations);
    }
    
    @GetMapping("/my-rides")
    public ResponseEntity<List<RideDTO>> getMyRides(HttpSession session) {
        try {
            // Verificăm dacă utilizatorul este logat
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<RideDTO> rides = rideService.getRidesByUser(user);
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/my-rides/active")
    public ResponseEntity<List<RideDTO>> getMyActiveRides(HttpSession session) {
        try {
            // Verificăm dacă utilizatorul este logat
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<RideDTO> rides = rideService.getActiveRidesByUser(user);
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/my-rides/completed")
    public ResponseEntity<List<RideDTO>> getMyCompletedRides(HttpSession session) {
        try {
            // Verificăm dacă utilizatorul este logat
            User user = (User) session.getAttribute("user");
            if (user == null) {
                return ResponseEntity.status(401).build();
            }
            
            List<RideDTO> rides = rideService.getCompletedRidesByUser(user);
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RideDTO>> getRidesByUserId(@PathVariable Long userId) {
        try {
            List<RideDTO> rides = rideService.getRidesByUserId(userId);
            return ResponseEntity.ok(rides);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/view")
    public ResponseEntity<Map<String, Object>> recordView(@PathVariable Long id, HttpServletRequest request) {
        try {
            // Obținem IP-ul clientului
            String clientIP = getClientIPAddress(request);
            
            // Înregistrăm vizualizarea
            rideViewService.recordView(id, clientIP);
            
            // Obținem numărul actualizat de vizualizări
            Long viewCount = rideViewService.getViewCount(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("viewCount", viewCount);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la înregistrarea vizualizării: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteRide(@PathVariable Long id, HttpSession session) {
        try {
            // Verificăm dacă utilizatorul este logat
            User user = (User) session.getAttribute("user");
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a șterge o cursă.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Ștergem cursa
            rideService.deleteRide(id, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cursa a fost ștearsă cu succes!");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la ștergerea cursei: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/{id}/edit")
    public ResponseEntity<Map<String, Object>> getRideForEdit(@PathVariable Long id, HttpSession session) {
        try {
            // Verificăm dacă utilizatorul este logat
            User user = (User) session.getAttribute("user");
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a edita o cursă.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Obținem cursa pentru editare
            RideDTO ride = rideService.getRideById(id);
            
            // Verificăm dacă cursa aparține utilizatorului
            if (!ride.getUserId().equals(user.getId())) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Nu aveți permisiunea de a edita această cursă.");
                return ResponseEntity.status(403).body(response);
            }
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("ride", ride);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la obținerea cursei pentru editare: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateRide(@PathVariable Long id, @RequestBody AddRideRequest request, HttpSession session) {
        try {
            // Verificăm dacă utilizatorul este logat
            User user = (User) session.getAttribute("user");
            if (user == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a edita o cursă.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Actualizăm cursa
            RideDTO updatedRide = rideService.updateRide(id, request, user);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Cursa a fost actualizată cu succes!");
            response.put("ride", updatedRide);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la actualizarea cursei: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
