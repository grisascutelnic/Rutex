package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Locality;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LocalityRepository extends JpaRepository<Locality, Long> {
    
    Optional<Locality> findByGooglePlaceId(String googlePlaceId);
    
    @Query("SELECT l FROM Locality l WHERE LOWER(l.nameRo) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(l.nameRu) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Locality> findByNameContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT l FROM Locality l WHERE LOWER(l.nameRo) LIKE LOWER(CONCAT(:query, '%')) OR LOWER(l.nameRu) LIKE LOWER(CONCAT(:query, '%'))")
    List<Locality> findByNameStartingWithIgnoreCase(@Param("query") String query);
    
    @Query("SELECT l FROM Locality l WHERE LOWER(l.nameRo) LIKE LOWER(CONCAT(:query, '%')) OR LOWER(l.nameRu) LIKE LOWER(CONCAT(:query, '%')) ORDER BY l.searchCount DESC, l.nameRo ASC")
    List<Locality> findByNameStartingWithIgnoreCaseOrderByPopularity(@Param("query") String query, Pageable pageable);
    
    // Metode pentru căutare în funcție de limbă
    @Query("SELECT l FROM Locality l WHERE LOWER(l.nameRo) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Locality> findByNameRoContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT l FROM Locality l WHERE LOWER(l.nameRu) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Locality> findByNameRuContainingIgnoreCase(@Param("query") String query);
    
    @Query("SELECT l FROM Locality l WHERE LOWER(l.nameRo) LIKE LOWER(CONCAT(:query, '%')) ORDER BY l.searchCount DESC, l.nameRo ASC")
    List<Locality> findByNameRoStartingWithIgnoreCaseOrderByPopularity(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT l FROM Locality l WHERE LOWER(l.nameRu) LIKE LOWER(CONCAT(:query, '%')) ORDER BY l.searchCount DESC, l.nameRu ASC")
    List<Locality> findByNameRuStartingWithIgnoreCaseOrderByPopularity(@Param("query") String query, Pageable pageable);
    
    @Query("SELECT l FROM Locality l WHERE l.district.id = :districtId")
    List<Locality> findByDistrictId(@Param("districtId") Long districtId);
    
    @Query("SELECT l FROM Locality l WHERE l.type = :type")
    List<Locality> findByType(@Param("type") Locality.LocalityType type);
    
    @Query("SELECT l FROM Locality l WHERE l.latitude BETWEEN :minLat AND :maxLat AND l.longitude BETWEEN :minLng AND :maxLng")
    List<Locality> findByBoundingBox(@Param("minLat") Double minLat, @Param("maxLat") Double maxLat, 
                                   @Param("minLng") Double minLng, @Param("maxLng") Double maxLng);
    
    @Query("SELECT l FROM Locality l ORDER BY l.searchCount DESC")
    Page<Locality> findMostPopular(Pageable pageable);
    
    boolean existsByGooglePlaceId(String googlePlaceId);
    
    @Query("SELECT COUNT(l) FROM Locality l WHERE l.district.id = :districtId")
    long countByDistrictId(@Param("districtId") Long districtId);
    
    @Query("SELECT l FROM Locality l LEFT JOIN FETCH l.district ORDER BY l.searchCount DESC")
    List<Locality> findAllWithDistrict();
}
