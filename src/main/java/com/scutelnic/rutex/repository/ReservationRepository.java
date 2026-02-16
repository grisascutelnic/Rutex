package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findTop200ByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Reservation r LEFT JOIN FETCH r.ride LEFT JOIN FETCH r.driver ORDER BY r.createdAt DESC")
    List<Reservation> findRecentWithRideAndDriver(Pageable pageable);

    @Modifying
    @Transactional
    @Query("DELETE FROM Reservation r WHERE r.ride.id = :rideId")
    void deleteByRideId(@Param("rideId") Long rideId);
}
