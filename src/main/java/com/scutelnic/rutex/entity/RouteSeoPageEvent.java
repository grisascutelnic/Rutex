package com.scutelnic.rutex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "route_seo_page_events")
@Data
public class RouteSeoPageEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "route_slug", nullable = false, length = 180)
    private String routeSlug;

    @Column(nullable = false, length = 2)
    private String language;

    @Column(name = "event_type", nullable = false, length = 40)
    private String eventType;

    @Column(name = "visitor_key", nullable = false, length = 80)
    private String visitorKey;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ride_id")
    private Long rideId;

    @Column(name = "page_url", length = 500)
    private String pageUrl;

    @Column(length = 500)
    private String referrer;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
