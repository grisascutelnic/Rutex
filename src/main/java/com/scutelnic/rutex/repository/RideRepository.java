package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Ride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface RideRepository extends JpaRepository<Ride, Long> {
    
    @Query("SELECT r FROM Ride r LEFT JOIN FETCH r.user WHERE r.isActive = true ORDER BY r.createdAt DESC")
    List<Ride> findAllActiveRides();
    
    @Query("SELECT r FROM Ride r JOIN FETCH r.user WHERE r.isActive = true AND r.fromLocation LIKE %:fromLocation% AND r.toLocation LIKE %:toLocation% AND r.travelDate >= :travelDate ORDER BY r.createdAt DESC")
    List<Ride> searchRides(@Param("fromLocation") String fromLocation, 
                           @Param("toLocation") String toLocation, 
                           @Param("travelDate") LocalDateTime travelDate);
    
    @Query("SELECT r FROM Ride r JOIN FETCH r.user WHERE r.isActive = true " +
           "AND (:fromLocation IS NULL OR r.fromLocation LIKE %:fromLocation%) " +
           "AND (:toLocation IS NULL OR r.toLocation LIKE %:toLocation%) " +
           "AND (:travelDate IS NULL OR (r.travelDate >= :travelDate AND r.travelDate < :travelDateEnd)) " +
           "ORDER BY r.createdAt DESC")
    List<Ride> searchRidesFlexible(@Param("fromLocation") String fromLocation, 
                                   @Param("toLocation") String toLocation, 
                                   @Param("travelDate") LocalDateTime travelDate,
                                   @Param("travelDateEnd") LocalDateTime travelDateEnd);
    
    @Query("SELECT r FROM Ride r JOIN FETCH r.user WHERE r.isActive = true AND r.availableSeats >= :minSeats ORDER BY r.createdAt DESC")
    List<Ride> findRidesByAvailableSeats(@Param("minSeats") Integer minSeats);
    
    @Query("SELECT DISTINCT r.fromLocation FROM Ride r WHERE r.isActive = true ORDER BY r.fromLocation")
    List<String> findAllFromLocations();
    
    @Query("SELECT DISTINCT r.toLocation FROM Ride r WHERE r.isActive = true ORDER BY r.toLocation")
    List<String> findAllToLocations();
    
    @Query("SELECT r FROM Ride r JOIN FETCH r.user WHERE r.user = :user ORDER BY r.createdAt DESC")
    List<Ride> findByUserOrderByCreatedAtDesc(@Param("user") com.scutelnic.rutex.entity.User user);
    
    @Query("SELECT r FROM Ride r JOIN FETCH r.user WHERE r.user.id = :userId ORDER BY r.createdAt DESC")
    List<Ride> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
    
    @Query("SELECT r FROM Ride r JOIN FETCH r.user WHERE r.isActive = true ORDER BY r.createdAt DESC LIMIT 5")
    List<Ride> findTop5RecentRides();
    
    // Statistics methods
    long countByCreatedAtAfter(LocalDateTime dateTime);
    long countByIsActiveTrue();

    @Query("SELECT r.user.id FROM Ride r WHERE r.id = :rideId")
    Long findOwnerIdByRideId(@Param("rideId") Long rideId);

    @Query("SELECT r FROM Ride r WHERE r.isActive = true AND r.travelDate < :currentDateTime")
    List<Ride> findExpiredRides(@Param("currentDateTime") LocalDateTime currentDateTime);

    List<Ride> findByVehicleId(Long vehicleId);
}
