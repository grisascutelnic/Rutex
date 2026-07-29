package com.scutelnic.rutex.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalTime;
import com.scutelnic.rutex.entity.AnnouncementType;

@Data
public class AddRideRequest {
    private String fromLocation;
    private String toLocation;
    private LocalDate travelDate;
    private LocalTime departureTime;
    private Integer availableSeats;
    private String description;
    private Boolean isPackageOnly = false;
    private Boolean transportAndPackages = false;
    private Long vehicleId;
    private AnnouncementType announcementType;
    private Integer requestedSeats;
    private Boolean flexibleTime = false;
}
