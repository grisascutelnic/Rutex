package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_view_ips")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideViewIP {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false)
    private Ride ride;
    
    @Column(nullable = false)
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime viewedAt;
    
    @PrePersist
    protected void onCreate() {
        if (viewedAt == null) {
            viewedAt = LocalDateTime.now();
        }
    }
}
