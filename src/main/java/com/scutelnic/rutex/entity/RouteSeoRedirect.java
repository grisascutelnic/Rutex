package com.scutelnic.rutex.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Table(
        name = "route_seo_redirects",
        uniqueConstraints = @UniqueConstraint(name = "uk_route_seo_redirect_old_slug", columnNames = "old_slug")
)
@Data
public class RouteSeoRedirect {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "old_slug", nullable = false, length = 180)
    private String oldSlug;

    @Column(name = "new_slug", nullable = false, length = 180)
    private String newSlug;
}
