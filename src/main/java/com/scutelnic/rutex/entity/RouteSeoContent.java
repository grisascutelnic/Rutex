package com.scutelnic.rutex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "route_seo_content",
        uniqueConstraints = @UniqueConstraint(name = "uk_route_seo_slug_language", columnNames = {"route_slug", "language"})
)
@Data
public class RouteSeoContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_slug", nullable = false, length = 180)
    private String routeSlug;

    @Column(nullable = false, length = 2)
    private String language;

    @Column(name = "from_location", nullable = false)
    private String fromLocation;

    @Column(name = "to_location", nullable = false)
    private String toLocation;

    @Column(name = "route_description", length = 2000)
    private String routeDescription;

    @Column(name = "from_description", length = 2000)
    private String fromDescription;

    @Column(name = "to_description", length = 2000)
    private String toDescription;

    @Column(name = "nearby_directions_text", length = 2000)
    private String nearbyDirectionsText;

    @Column(name = "frequent_searches_text", length = 2000)
    private String frequentSearchesText;

    @Column(length = 40)
    private String source;

    @Column(name = "admin_verified", nullable = false)
    private boolean adminVerified;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
