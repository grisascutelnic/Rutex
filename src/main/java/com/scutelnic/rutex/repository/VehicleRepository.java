package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.entity.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
    List<Vehicle> findByUserOrderByCreatedAtDesc(User user);
    Optional<Vehicle> findByIdAndUser(Long id, User user);
}
