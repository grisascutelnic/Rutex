package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.ReservationRequest;
import com.scutelnic.rutex.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private static final Logger logger = LoggerFactory.getLogger(ReservationController.class);

    @Autowired
    private ReservationService reservationService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> createReservation(@RequestBody ReservationRequest request,
                                                                 HttpServletRequest httpRequest) {
        try {
            reservationService.createReservation(request, httpRequest);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Rezervarea a fost trimisă cu succes.");
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            logger.error("Unexpected error while creating reservation for rideId={}", request != null ? request.getRideId() : null, e);
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la trimiterea rezervării.");
            return ResponseEntity.badRequest().body(response);
        }
    }
}
