package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findTop200ByOrderByCreatedAtDesc();
    long countByRideId(Long rideId);
    @Query("select coalesce(sum(r.passengerCount), 0) from Reservation r where r.ride.id = :rideId")
    Long sumPassengerCountByRideId(@Param("rideId") Long rideId);
}
