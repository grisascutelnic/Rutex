package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {
    
    Optional<District> findByGooglePlaceId(String googlePlaceId);
    
    @Query("SELECT d FROM District d WHERE LOWER(d.nameRo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(d.nameRu) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<District> findByNameContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT d FROM District d WHERE LOWER(d.nameRo) LIKE LOWER(CONCAT(:query, '%')) OR LOWER(d.nameRu) LIKE LOWER(CONCAT(:query, '%'))")
    List<District> findByNameStartingWithIgnoreCase(@Param("query") String query);
    
    boolean existsByGooglePlaceId(String googlePlaceId);
}
