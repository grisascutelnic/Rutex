package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "rides")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ride {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String fromLocation;
    
    @Column(nullable = false)
    private String toLocation;
    
    @Column(nullable = false)
    private LocalDateTime departureTime;
    
    @Column(nullable = false)
    private LocalDateTime travelDate;
    
    @Column(nullable = false)
    private Integer availableSeats;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vehicle_id")
    private Vehicle vehicle;

    @Column
    private String vehicleMake;

    @Column
    private String vehicleColor;

    @Column
    private String vehiclePlateNumber;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private Boolean isActive;
    
    @Column(nullable = false)
    private Boolean isPackageOnly = false;
    
    @Column(nullable = false)
    private Boolean transportAndPackages = false;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        isActive = true;
        if (isPackageOnly == null) {
            isPackageOnly = false;
        }
        if (transportAndPackages == null) {
            transportAndPackages = false;
        }
    }
}
