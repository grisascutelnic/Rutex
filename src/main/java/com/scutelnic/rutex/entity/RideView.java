package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "ride_views")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideView {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ride_id", nullable = false, unique = true)
    private Ride ride;
    
    @Column(nullable = false)
    private Long viewCount = 0L;
    
    @Column(nullable = false)
    private LocalDateTime lastViewed;
    
    @PrePersist
    protected void onCreate() {
        if (lastViewed == null) {
            lastViewed = LocalDateTime.now();
        }
        if (viewCount == null) {
            viewCount = 0L;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastViewed = LocalDateTime.now();
    }
}
