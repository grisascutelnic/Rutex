package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.RouteSeoRedirect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RouteSeoRedirectRepository extends JpaRepository<RouteSeoRedirect, Long> {

    Optional<RouteSeoRedirect> findByOldSlug(String oldSlug);
}
