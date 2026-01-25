package com.scutelnic.rutex.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleDTO {
    private Long id;
    private String make;
    private String color;
    private String plateNumber;
    private Boolean isDefault;
}
