package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.RideViewIP;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RideViewIPRepository extends JpaRepository<RideViewIP, Long> {
    
    @Query("SELECT rvip FROM RideViewIP rvip WHERE rvip.ride.id = :rideId AND rvip.ipAddress = :ipAddress")
    Optional<RideViewIP> findByRideIdAndIpAddress(@Param("rideId") Long rideId, @Param("ipAddress") String ipAddress);
    
    @Query("SELECT COUNT(rvip) FROM RideViewIP rvip WHERE rvip.ride.id = :rideId")
    Long countByRideId(@Param("rideId") Long rideId);
    
    @Modifying
    @Query("DELETE FROM RideViewIP rvip WHERE rvip.ride.id = :rideId")
    int deleteByRideId(@Param("rideId") Long rideId);
}
