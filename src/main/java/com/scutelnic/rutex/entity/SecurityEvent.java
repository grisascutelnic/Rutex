package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private String eventType; // SUSPICIOUS_IP, SUSPICIOUS_USER_AGENT, RATE_LIMIT_EXCEEDED, etc.
    
    @Column(nullable = false)
    private String description;
    
    @Column
    private String userAgent;
    
    @Column
    private String requestUrl;
    
    @Column
    private String requestMethod;
    
    @Column(nullable = false)
    private LocalDateTime timestamp;
    
    @Column
    private Boolean isResolved = false;
    
    @Column
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    
    @Column
    private Integer requestCount;
    
    @Column
    private String country;
    
    @Column
    private String city;
}
