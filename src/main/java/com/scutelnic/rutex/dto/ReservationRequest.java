package com.scutelnic.rutex.dto;

import lombok.Data;

@Data
public class ReservationRequest {
    private Long rideId;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private Integer passengerCount;
    private String additionalInfo;
    private Boolean consentToShareData;
    private String language;
}
