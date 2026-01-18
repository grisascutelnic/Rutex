package com.scutelnic.rutex.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class AddRideRequest {
    private String fromLocation;
    private String toLocation;
    private LocalDate travelDate;
    private LocalTime departureTime;
    private Integer availableSeats;
    private BigDecimal price;
    private String description;
    private Boolean isPackageOnly = false;
    private Boolean transportAndPackages = false;
}
