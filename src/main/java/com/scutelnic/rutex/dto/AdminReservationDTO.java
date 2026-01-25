package com.scutelnic.rutex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminReservationDTO {
    private Long id;
    private LocalDateTime createdAt;
    private Long rideId;
    private String fromLocation;
    private String toLocation;
    private LocalDateTime travelDate;
    private LocalDateTime departureTime;
    private Long driverId;
    private String driverName;
    private String driverEmail;
    private String passengerFirstName;
    private String passengerLastName;
    private String passengerEmail;
    private String passengerPhone;
    private Integer passengerCount;
    private String additionalInfo;
    private Boolean consentToShareData;
}
