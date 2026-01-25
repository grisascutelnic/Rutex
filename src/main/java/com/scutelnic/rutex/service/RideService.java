package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.entity.Vehicle;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.dto.SearchRideRequest;
import com.scutelnic.rutex.dto.AddRideRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.time.ZoneId;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RideService {
    
    @Autowired
    private RideRepository rideRepository;
    
    @Autowired
    private RideViewService rideViewService;

    @Autowired
    private VehicleService vehicleService;
    
    // Removed unused flags and deprecated maintenance helpers
    
    public List<RideDTO> getAllActiveRides() {
        try {
            System.out.println("Fetching all active rides from database...");
            
            // Marchem calatoriile completate ca inactive inainte de a obtine toate calatoriile
            markCompletedRidesAsInactive();
            
            List<Ride> rides = rideRepository.findAllActiveRides();
            System.out.println("Found " + rides.size() + " active rides in database");
            
            List<RideDTO> rideDTOs = rides.stream()
                       .map(this::convertToDTO)
                       .collect(Collectors.toList());
            
            return rideDTOs;
        } catch (Exception e) {
            System.err.println("Error fetching rides: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    public List<RideDTO> searchRides(SearchRideRequest request) {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        
        // Verificăm și actualizăm câmpurile pentru toate cursele (la fiecare căutare)
        updateAllRidesPackageFields();
        
        // Marchem calatoriile completate ca inactive inainte de cautare
        markCompletedRidesAsInactive();
        
        // Construim query-ul în funcție de câmpurile furnizate
        String fromLocation = request.getFromLocation();
        String toLocation = request.getToLocation();
        LocalDateTime travelDateTime = null;
        
        if (request.getTravelDate() != null) {
            travelDateTime = request.getTravelDate().atStartOfDay();
        }
        
        // Search rides request processing
        
        LocalDateTime travelDateTimeEnd = null;
        if (travelDateTime != null) {
            travelDateTimeEnd = travelDateTime.plusDays(1);
        }
        
        List<Ride> rides = rideRepository.searchRidesFlexible(
            fromLocation,
            toLocation,
            travelDateTime,
            travelDateTimeEnd
        );
        
        // Search found rides
        
        // Filtrare suplimentară pentru locuri disponibile sau curse de colete
        if (request.getPassengers() != null) {
            rides = rides.stream()
                        .filter(ride -> {
                            // Dacă passengers = 0 (colete bifat), vrem doar curse de colete (exclude cursele simple)
                            if (request.getPassengers() == 0) {
                                return ride.getIsPackageOnly() || ride.getTransportAndPackages();
                            }
                            // Dacă passengers != 0 (colete debifat), exclude "Transport doar colete"
                            if (ride.getIsPackageOnly()) {
                                return false; // Exclude "Transport doar colete"
                            }
                            // Pentru transport pasageri, verificăm locurile disponibile
                            boolean hasEnoughSeats = ride.getAvailableSeats() >= request.getPassengers();
                            // Passenger ride processed
                            return hasEnoughSeats;
                        })
                        .collect(Collectors.toList());
        } else {
            // Dacă nu există parametru passengers, exclude "Transport doar colete" (colete debifat implicit)
            rides = rides.stream()
                        .filter(ride -> !ride.getIsPackageOnly()) // Exclude "Transport doar colete"
                        .collect(Collectors.toList());
        }
        
        List<RideDTO> result = rides.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());

        return result;
    }
    
    @Transactional
    public RideDTO addRide(AddRideRequest request, User user) {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        
        // Adding new ride
        // Request parameters processed
        
        Ride ride = new Ride();
        ride.setFromLocation(request.getFromLocation());
        ride.setToLocation(request.getToLocation());
        ride.setTravelDate(request.getTravelDate().atStartOfDay());
        ride.setDepartureTime(LocalDateTime.of(request.getTravelDate(), request.getDepartureTime()));
        ride.setAvailableSeats(request.getAvailableSeats());
        ride.setDescription(request.getDescription());
        ride.setIsPackageOnly(request.getIsPackageOnly() != null ? request.getIsPackageOnly() : false);
        ride.setTransportAndPackages(request.getTransportAndPackages() != null ? request.getTransportAndPackages() : false);
        ride.setUser(user);

        if (request.getVehicleId() == null) {
            throw new RuntimeException("Selectați un vehicul pentru această cursă.");
        }
        Vehicle vehicle = vehicleService.getVehicleForUser(request.getVehicleId(), user);
        ride.setVehicle(vehicle);
        ride.setVehicleMake(vehicle.getMake());
        ride.setVehicleColor(vehicle.getColor());
        ride.setVehiclePlateNumber(vehicle.getPlateNumber());
        
        // Ride before save
        
        Ride savedRide = rideRepository.save(ride);
        
        // Ride after save
        // Ride added successfully
        
        return convertToDTO(savedRide);
    }
    
    public RideDTO getRideById(Long id) {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        
        Ride ride = rideRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cursa nu a fost găsită"));
        return convertToDTO(ride);
    }
    
    public List<String> getAllFromLocations() {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        return rideRepository.findAllFromLocations();
    }
    
    public List<String> getAllToLocations() {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        return rideRepository.findAllToLocations();
    }
    
    public List<RideDTO> getRidesByUser(User user) {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        
        List<Ride> rides = rideRepository.findByUserOrderByCreatedAtDesc(user);
        return rides.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }
    
    /**
     * Obtine calatoriile active ale unui utilizator (care nu au trecut data si ora)
     */
    public List<RideDTO> getActiveRidesByUser(User user) {
        List<Ride> rides = rideRepository.findByUserOrderByCreatedAtDesc(user);
        return rides.stream()
                   .filter(Ride::getIsActive)
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }
    
    /**
     * Obtine calatoriile completate ale unui utilizator (care au trecut data si ora)
     */
    public List<RideDTO> getCompletedRidesByUser(User user) {
        List<Ride> rides = rideRepository.findByUserOrderByCreatedAtDesc(user);
        return rides.stream()
                   .filter(ride -> !ride.getIsActive())
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }
    
    public List<RideDTO> getRidesByUserId(Long userId) {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        
        List<Ride> rides = rideRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return rides.stream()
                   .map(this::convertToDTO)
                   .collect(Collectors.toList());
    }
    
    public List<RideDTO> getTop5RecentRides() {
        try {
            // Marchem calatoriile completate ca inactive inainte de a obtine top 5
            markCompletedRidesAsInactive();
            
            List<Ride> rides = rideRepository.findTop5RecentRides();
            return rides.stream()
                       .limit(5)
                       .map(this::convertToDTO)
                       .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error fetching top 5 recent rides: " + e.getMessage());
            e.printStackTrace();
            return new ArrayList<>();
        }
    }
    
    /**
     * Curăță automat cursele care au trecut data de călătorie + 1 zi
     * Folosește timpul din România (Europe/Bucharest)
     * Această metodă trebuie apelată periodic, nu la fiecare cerere
     */
    @Transactional
    public void cleanupExpiredRides() {
        // Folosim timpul din România/București
        ZoneId bucharestZone = ZoneId.of("Europe/Bucharest");
        LocalDateTime currentDateTime = LocalDateTime.now(bucharestZone);
        
        // Găsim toate cursele active
        List<Ride> activeRides = rideRepository.findAll().stream()
                .filter(Ride::getIsActive)
                .collect(Collectors.toList());
        
        List<Ride> expiredRides = new ArrayList<>();
        
        for (Ride ride : activeRides) {
            // Verificăm dacă a trecut mai mult de 1 zi de la data și ora cursei
            // ride.getTravelDate() conține deja data și ora de plecare
            if (currentDateTime.isAfter(ride.getTravelDate().plusDays(1))) {
                expiredRides.add(ride);
            }
        }
        
        if (!expiredRides.isEmpty()) {
            // Setăm cursele ca inactive în loc să le ștergem
            expiredRides.forEach(ride -> ride.setIsActive(false));
            rideRepository.saveAll(expiredRides);
            System.out.println("Curse expirate setate ca inactive: " + expiredRides.size());
        }
    }
    
    /**
     * Marcheaza automat calatoriile care au trecut data si ora programata ca inactive
     * Foloseste timpul din România (Europe/Bucharest)
     * Aceasta metoda ruleaza la fiecare 5 minute prin scheduler
     */
    @Transactional
    public void markCompletedRidesAsInactive() {
        // Folosim timpul din România/București
        ZoneId bucharestZone = ZoneId.of("Europe/Bucharest");
        LocalDateTime currentDateTime = LocalDateTime.now(bucharestZone);
        
        // Gasim toate calatoriile active
        List<Ride> activeRides = rideRepository.findAll().stream()
                .filter(Ride::getIsActive)
                .collect(Collectors.toList());
        
        List<Ride> completedRides = new ArrayList<>();
        
        for (Ride ride : activeRides) {
            // Verificam daca a trecut data si ora de plecare
            if (currentDateTime.isAfter(ride.getDepartureTime())) {
                completedRides.add(ride);
            }
        }
        
        if (!completedRides.isEmpty()) {
            // Setam calatoriile ca inactive
            completedRides.forEach(ride -> ride.setIsActive(false));
            rideRepository.saveAll(completedRides);
            System.out.println("Calatorii completate setate ca inactive: " + completedRides.size());
            
            // Logging pentru fiecare calatorie marcata ca completata
            for (Ride ride : completedRides) {
                System.out.println("Calatorie completata - ID: " + ride.getId() + 
                                 ", De la: " + ride.getFromLocation() + 
                                 ", La: " + ride.getToLocation() + 
                                 ", Data plecare: " + ride.getDepartureTime());
            }
        }
    }
    
    
    
    @Transactional
    public void deleteRide(Long rideId, User user) {
        // Nu curățăm automat cursele expirate aici pentru a nu afecta performanța
        
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Cursa nu a fost găsită"));
        
        // Verificăm dacă utilizatorul este proprietarul cursei
        if (!ride.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Nu aveți permisiunea de a șterge această cursă");
        }
        
        // Ștergem vizualizările pentru cursa
        rideViewService.deleteViewsForRide(rideId);
        
        // Ștergem cursa
        rideRepository.delete(ride);
    }
    
    /**
     * Actualizează o cursă (doar proprietarul poate edita)
     */
    @Transactional
    public RideDTO updateRide(Long rideId, AddRideRequest request, User user) {
        Ride ride = rideRepository.findById(rideId)
                .orElseThrow(() -> new RuntimeException("Cursa nu a fost găsită"));
        
        // Verificăm dacă utilizatorul este proprietarul cursei
        if (!ride.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Nu aveți permisiunea de a edita această cursă");
        }
        
        // Actualizăm datele cursei
        ride.setFromLocation(request.getFromLocation());
        ride.setToLocation(request.getToLocation());
        ride.setTravelDate(request.getTravelDate().atStartOfDay());
        ride.setDepartureTime(LocalDateTime.of(request.getTravelDate(), request.getDepartureTime()));
        ride.setAvailableSeats(request.getAvailableSeats());
        ride.setDescription(request.getDescription());
        ride.setIsPackageOnly(request.getIsPackageOnly() != null ? request.getIsPackageOnly() : false);
        ride.setTransportAndPackages(request.getTransportAndPackages() != null ? request.getTransportAndPackages() : false);
        if (request.getVehicleId() != null) {
            Vehicle vehicle = vehicleService.getVehicleForUser(request.getVehicleId(), user);
            ride.setVehicle(vehicle);
            ride.setVehicleMake(vehicle.getMake());
            ride.setVehicleColor(vehicle.getColor());
            ride.setVehiclePlateNumber(vehicle.getPlateNumber());
        }
        
        // Salvăm cursa actualizată
        Ride updatedRide = rideRepository.save(ride);
        
        // Returnăm DTO-ul actualizat
        return convertToDTO(updatedRide);
    }
    
    
    
    /**
     * Actualizează câmpurile pentru toate cursele
     */
    private void updateAllRidesPackageFields() {
        try {
            List<Ride> allRides = rideRepository.findAll();
            boolean needsUpdate = false;
            
            // Updating all rides package fields
            // Checking rides
            
            for (Ride ride : allRides) {
                // Verificăm dacă câmpurile sunt null sau diferite de false
                if (ride.getIsPackageOnly() == null) {
                    ride.setIsPackageOnly(false);
                    needsUpdate = true;
                    System.out.println("Updated ride " + ride.getId() + " - set isPackageOnly to false");
                }
                if (ride.getTransportAndPackages() == null) {
                    ride.setTransportAndPackages(false);
                    needsUpdate = true;
                    System.out.println("Updated ride " + ride.getId() + " - set transportAndPackages to false");
                }
            }
            
            if (needsUpdate) {
                rideRepository.saveAll(allRides);
            }
        } catch (Exception e) {
            System.err.println("Error updating rides package fields: " + e.getMessage());
        }
    }
    
    
    
    
    
    
    
    private RideDTO convertToDTO(Ride ride) {
        if (ride == null) {
            return null;
        }
        
        // Converting ride to DTO
        
        // Setăm valori implicite pentru câmpurile care ar putea fi null
        Boolean isPackageOnly = ride.getIsPackageOnly() != null ? ride.getIsPackageOnly() : false;
        Boolean transportAndPackages = ride.getTransportAndPackages() != null ? ride.getTransportAndPackages() : false;
        
        // After null check
        
        User user = ride.getUser();
        Vehicle vehicle = ride.getVehicle();
        Long vehicleId = vehicle != null ? vehicle.getId() : null;
        String vehicleMake = ride.getVehicleMake() != null ? ride.getVehicleMake()
                : (vehicle != null ? vehicle.getMake() : null);
        String vehicleColor = ride.getVehicleColor() != null ? ride.getVehicleColor()
                : (vehicle != null ? vehicle.getColor() : null);
        String vehiclePlate = ride.getVehiclePlateNumber() != null ? ride.getVehiclePlateNumber()
                : (vehicle != null ? vehicle.getPlateNumber() : null);
        
        // Obținem numărul de vizualizări
        Long viewCount = rideViewService.getViewCount(ride.getId());
        
        if (user == null || user.getId() == null) {
            // Dacă user-ul este null sau nu are ID, returnăm un DTO cu informații minime
            return new RideDTO(
                ride.getId(),
                ride.getFromLocation(),
                ride.getToLocation(),
                ride.getDepartureTime(),
                ride.getTravelDate(),
                ride.getAvailableSeats(),
                ride.getDescription(),
                0L, // ID-ul utilizatorului nu poate fi 0, deci nu va fi clickabil
                "Utilizator necunoscut",
                "N/A",
                "N/A",
                null,
                vehicleId,
                vehicleMake,
                vehicleColor,
                vehiclePlate,
                ride.getCreatedAt(),
                ride.getIsActive(),
                isPackageOnly,
                transportAndPackages,
                viewCount
            );
        }
        
        return new RideDTO(
            ride.getId(),
            ride.getFromLocation(),
            ride.getToLocation(),
            ride.getDepartureTime(),
            ride.getTravelDate(),
            ride.getAvailableSeats(),
            ride.getDescription(),
            user.getId(),
            user.getFirstName() + " " + user.getLastName(),
            correctPhoneNumber(user.getPhone()),
            user.getEmail(),
            user.getProfileImage(),
            vehicleId,
            vehicleMake,
            vehicleColor,
            vehiclePlate,
            ride.getCreatedAt(),
            ride.getIsActive(),
            isPackageOnly,
            transportAndPackages,
            viewCount
        );
    }
    
    /**
     * Get ride statistics for admin dashboard
     */
    public Map<String, Object> getRideStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        try {
            // Total rides
            long totalRides = rideRepository.count();
            stats.put("totalRides", totalRides);
            
            // Today's rides
            LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
            long todayRides = rideRepository.countByCreatedAtAfter(todayStart);
            stats.put("todayRides", todayRides);
            
            // This week's rides
            LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
            long weekRides = rideRepository.countByCreatedAtAfter(weekStart);
            stats.put("weekRides", weekRides);
            
            // This month's rides
            LocalDateTime monthStart = LocalDateTime.now().minusDays(30);
            long monthRides = rideRepository.countByCreatedAtAfter(monthStart);
            stats.put("monthRides", monthRides);
            
            // Active rides
            long activeRides = rideRepository.countByIsActiveTrue();
            stats.put("activeRides", activeRides);
            
        } catch (Exception e) {
            System.err.println("Error getting ride statistics: " + e.getMessage());
            e.printStackTrace();
            // Return default values
            stats.put("totalRides", 0L);
            stats.put("todayRides", 0L);
            stats.put("weekRides", 0L);
            stats.put("monthRides", 0L);
            stats.put("activeRides", 0L);
        }
        
        return stats;
    }
    
    /**
     * Formatează numărul de telefon cu prefixul țării pentru afișare
     * @param phone - Numărul de telefon (poate fi cu sau fără prefix)
     * @return Numărul formatat cu prefix pentru afișare
     */
    
    
    /**
     * Corectează numerele de telefon salvate greșit în baza de date
     * Această metodă va fi apelată pentru a corecta numerele existente
     */
    public String correctPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        // Correcting phone number in RideService
        
        // Eliminăm toate caracterele care nu sunt cifre
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        // Digits extracted in RideService
        
        // Dacă numărul începe cu 0 și are 9 cifre, este un număr Moldova
        if (digitsOnly.startsWith("0") && digitsOnly.length() == 9) {
            String result = "+373 " + digitsOnly.substring(1);
            // Corrected 0 number in RideService
            return result;
        }
        
        // Dacă numărul începe cu 67, 62, 60, etc. și are 8 cifre, este un număr Moldova
        if (digitsOnly.length() == 8 && (digitsOnly.startsWith("6") || digitsOnly.startsWith("7"))) {
            String result = "+373 " + digitsOnly;
            // Corrected Moldova number in RideService
            return result;
        }
        
        // Dacă numărul începe cu 373 și are 11 cifre, este corect
        if (digitsOnly.startsWith("373") && digitsOnly.length() == 11) {
            String result = "+373 " + digitsOnly.substring(3);
            // Corrected 373 number in RideService
            return result;
        }
        
        // Pentru alte cazuri, returnăm formatul original
        return phone;
    }
}
