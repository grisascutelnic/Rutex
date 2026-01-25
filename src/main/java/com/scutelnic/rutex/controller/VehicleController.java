package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.AddVehicleRequest;
import com.scutelnic.rutex.dto.VehicleDTO;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.VehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/vehicles")
public class VehicleController {

    @Autowired
    private VehicleService vehicleService;

    @GetMapping
    public ResponseEntity<List<VehicleDTO>> getMyVehicles(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        return ResponseEntity.ok(vehicleService.getUserVehicles(user));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<VehicleDTO>> getVehiclesByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(vehicleService.getVehiclesByUserId(userId));
        } catch (RuntimeException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addVehicle(@RequestBody AddVehicleRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fiți logat pentru a adăuga un vehicul.");
            return ResponseEntity.status(401).body(response);
        }

        if (request.getMake() == null || request.getMake().trim().isEmpty()
                || request.getColor() == null || request.getColor().trim().isEmpty()
                || request.getPlateNumber() == null || request.getPlateNumber().trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Completați toate câmpurile vehiculului.");
            return ResponseEntity.badRequest().body(response);
        }

        VehicleDTO saved = vehicleService.addVehicle(request, user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Vehiculul a fost adăugat cu succes.");
        response.put("vehicle", saved);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateVehicle(@PathVariable Long id, @RequestBody AddVehicleRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fiți logat pentru a edita un vehicul.");
            return ResponseEntity.status(401).body(response);
        }

        if (request.getMake() == null || request.getMake().trim().isEmpty()
                || request.getColor() == null || request.getColor().trim().isEmpty()
                || request.getPlateNumber() == null || request.getPlateNumber().trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Completați toate câmpurile vehiculului.");
            return ResponseEntity.badRequest().body(response);
        }

        VehicleDTO updated = vehicleService.updateVehicle(id, request, user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Vehiculul a fost actualizat.");
        response.put("vehicle", updated);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteVehicle(@PathVariable Long id, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fiți logat pentru a șterge un vehicul.");
            return ResponseEntity.status(401).body(response);
        }

        Map<String, Object> response = new HashMap<>();
        try {
            vehicleService.deleteVehicle(id, user);
            response.put("success", true);
            response.put("message", "Vehiculul a fost șters.");
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            response.put("success", false);
            response.put("message", ex.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
