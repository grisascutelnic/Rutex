package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.RideView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RideViewRepository extends JpaRepository<RideView, Long> {
    
    Optional<RideView> findByRideId(Long rideId);
    
    @Query("SELECT rv FROM RideView rv WHERE rv.ride.id = :rideId")
    Optional<RideView> findByRideIdQuery(@Param("rideId") Long rideId);
}
