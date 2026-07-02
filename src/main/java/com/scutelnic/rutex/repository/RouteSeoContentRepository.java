package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.RouteSeoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteSeoContentRepository extends JpaRepository<RouteSeoContent, Long> {

    Optional<RouteSeoContent> findByRouteSlugAndLanguage(String routeSlug, String language);
}
