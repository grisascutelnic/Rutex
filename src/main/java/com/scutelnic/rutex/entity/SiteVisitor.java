package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "site_visitors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SiteVisitor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime firstVisit;
    
    @Column(nullable = false)
    private LocalDateTime lastVisit;
    
    @Column(nullable = false)
    private Integer visitCount = 1;
    
    @Column
    private String userAgent;
    
    @Column
    private String country;
    
    @Column
    private String city;
    
    @Column
    private String region;
    
    @Column
    private String timezone;
    
    @Column
    private Boolean isActive = true;
    
    @Column(name = "banned")
    private Boolean banned = false;
}
