package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
public class Statistics {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "stat_key", nullable = false, unique = true)
    private String statKey;
    
    @Column(name = "stat_value", nullable = false)
    private Long statValue;
    
    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public Statistics() {}
    
    public Statistics(String statKey, Long statValue) {
        this.statKey = statKey;
        this.statValue = statValue;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getStatKey() {
        return statKey;
    }
    
    public void setStatKey(String statKey) {
        this.statKey = statKey;
    }
    
    public Long getStatValue() {
        return statValue;
    }
    
    public void setStatValue(Long statValue) {
        this.statValue = statValue;
    }
    
    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }
    
    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
