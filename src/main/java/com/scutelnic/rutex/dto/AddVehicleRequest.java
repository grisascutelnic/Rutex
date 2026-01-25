package com.scutelnic.rutex.dto;

import lombok.Data;

@Data
public class AddVehicleRequest {
    private String make;
    private String color;
    private String plateNumber;
    private Boolean isDefault = false;
}
