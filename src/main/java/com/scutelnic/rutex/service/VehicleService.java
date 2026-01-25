package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.AddVehicleRequest;
import com.scutelnic.rutex.dto.VehicleDTO;
import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.entity.Vehicle;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.repository.UserRepository;
import com.scutelnic.rutex.repository.VehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class VehicleService {

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserRepository userRepository;

    public List<VehicleDTO> getUserVehicles(User user) {
        return vehicleRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public List<VehicleDTO> getVehiclesByUserId(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit."));
        return getUserVehicles(user);
    }

    @Transactional
    public VehicleDTO addVehicle(AddVehicleRequest request, User user) {
        Vehicle vehicle = new Vehicle();
        vehicle.setUser(user);
        vehicle.setMake(request.getMake());
        vehicle.setColor(request.getColor());
        vehicle.setPlateNumber(request.getPlateNumber());

        vehicle.setIsDefault(false);

        Vehicle saved = vehicleRepository.save(vehicle);
        return toDto(saved);
    }

    public Vehicle getVehicleForUser(Long vehicleId, User user) {
        return vehicleRepository.findByIdAndUser(vehicleId, user)
                .orElseThrow(() -> new RuntimeException("Vehiculul nu a fost găsit pentru acest utilizator."));
    }

    @Transactional
    public VehicleDTO updateVehicle(Long vehicleId, AddVehicleRequest request, User user) {
        Vehicle vehicle = vehicleRepository.findByIdAndUser(vehicleId, user)
                .orElseThrow(() -> new RuntimeException("Vehiculul nu a fost găsit pentru acest utilizator."));

        if (request.getMake() != null && !request.getMake().trim().isEmpty()) {
            vehicle.setMake(request.getMake().trim());
        }
        if (request.getColor() != null && !request.getColor().trim().isEmpty()) {
            vehicle.setColor(request.getColor().trim());
        }
        if (request.getPlateNumber() != null && !request.getPlateNumber().trim().isEmpty()) {
            vehicle.setPlateNumber(request.getPlateNumber().trim());
        }

        Vehicle saved = vehicleRepository.save(vehicle);
        return toDto(saved);
    }

    @Transactional
    public void deleteVehicle(Long vehicleId, User user) {
        Vehicle vehicle = vehicleRepository.findByIdAndUser(vehicleId, user)
                .orElseThrow(() -> new RuntimeException("Vehiculul nu a fost găsit pentru acest utilizator."));

        List<Ride> ridesUsingVehicle = rideRepository.findByVehicleId(vehicleId);
        if (!ridesUsingVehicle.isEmpty()) {
            ridesUsingVehicle.forEach(ride -> ride.setVehicle(null));
            rideRepository.saveAll(ridesUsingVehicle);
        }

        vehicleRepository.delete(vehicle);
    }

    private VehicleDTO toDto(Vehicle vehicle) {
        if (vehicle == null) {
            return null;
        }
        return new VehicleDTO(
                vehicle.getId(),
                vehicle.getMake(),
                vehicle.getColor(),
                vehicle.getPlateNumber(),
                vehicle.getIsDefault()
        );
    }
}
