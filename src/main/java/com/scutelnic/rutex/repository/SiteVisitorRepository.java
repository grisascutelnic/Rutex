package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.SiteVisitor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SiteVisitorRepository extends JpaRepository<SiteVisitor, Long> {
    
    Optional<SiteVisitor> findByIpAddress(String ipAddress);
    
    @Query("SELECT COUNT(sv) FROM SiteVisitor sv WHERE sv.isActive = true")
    Long countActiveVisitors();
    
    @Query("SELECT COUNT(sv) FROM SiteVisitor sv WHERE sv.lastVisit >= :since AND sv.isActive = true")
    Long countVisitorsSince(@Param("since") java.time.LocalDateTime since);
    
    @Query("SELECT sv FROM SiteVisitor sv WHERE sv.isActive = true ORDER BY sv.lastVisit DESC")
    List<SiteVisitor> findAllActiveVisitorsOrderByLastVisit();
    
    @Query("SELECT sv FROM SiteVisitor sv WHERE sv.isActive = true ORDER BY sv.visitCount DESC")
    List<SiteVisitor> findAllActiveVisitorsOrderByVisitCount();
    
    @Query("SELECT sv FROM SiteVisitor sv WHERE sv.isActive = true ORDER BY sv.firstVisit DESC")
    List<SiteVisitor> findAllActiveVisitorsOrderByFirstVisit();
    
    @Query("SELECT sv FROM SiteVisitor sv WHERE sv.isActive = true ORDER BY sv.id DESC")
    List<SiteVisitor> findAllActiveVisitorsOrderByIdDesc();
    
    @Query("SELECT COUNT(sv) FROM SiteVisitor sv WHERE sv.country = :country AND sv.isActive = true")
    Long countVisitorsByCountry(@Param("country") String country);
    
    @Query("SELECT sv.country, COUNT(sv) FROM SiteVisitor sv WHERE sv.isActive = true GROUP BY sv.country ORDER BY COUNT(sv) DESC")
    List<Object[]> getVisitorsByCountry();
    
    // Ban-related queries
    List<SiteVisitor> findByBannedTrueOrderByLastVisitDesc();
    
    Optional<SiteVisitor> findByIpAddressAndBannedTrue(String ipAddress);
    
    @Query("SELECT COUNT(sv) FROM SiteVisitor sv WHERE sv.banned = true")
    Long countBannedVisitors();
}
