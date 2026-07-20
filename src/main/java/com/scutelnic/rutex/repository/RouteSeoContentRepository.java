package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.RouteSeoContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface RouteSeoContentRepository extends JpaRepository<RouteSeoContent, Long> {

    Optional<RouteSeoContent> findByRouteSlugAndLanguage(String routeSlug, String language);

    List<RouteSeoContent> findByLanguageOrderByUpdatedAtDesc(String language);

    List<RouteSeoContent> findAllByRouteSlug(String routeSlug);

    boolean existsByRouteSlugAndHiddenTrue(String routeSlug);
}
