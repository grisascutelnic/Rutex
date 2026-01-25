package com.scutelnic.rutex.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideDTO {
    private Long id;
    private String fromLocation;
    private String toLocation;
    private LocalDateTime departureTime;
    private LocalDateTime travelDate;
    private Integer availableSeats;
    private String description;
    private Long userId;
    private String driverName;
    private String driverPhone;
    private String driverEmail;
    private String driverProfileImage;
    private Long vehicleId;
    private String vehicleMake;
    private String vehicleColor;
    private String vehiclePlateNumber;
    private LocalDateTime createdAt;
    private Boolean isActive;
    private Boolean isPackageOnly;
    private Boolean transportAndPackages;
    private Long viewCount;
}
