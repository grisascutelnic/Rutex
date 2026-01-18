package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.SecurityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SecurityEventRepository extends JpaRepository<SecurityEvent, Long> {
    
    List<SecurityEvent> findByIpAddressOrderByTimestampDesc(String ipAddress);
    
    List<SecurityEvent> findByEventTypeOrderByTimestampDesc(String eventType);
    
    List<SecurityEvent> findBySeverityOrderByTimestampDesc(String severity);
    
    List<SecurityEvent> findByIsResolvedFalseOrderByTimestampDesc();
    
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.ipAddress = :ipAddress AND se.timestamp >= :since")
    Long countEventsByIpSince(@Param("ipAddress") String ipAddress, @Param("since") LocalDateTime since);
    
    @Query("SELECT se FROM SecurityEvent se WHERE se.timestamp >= :since ORDER BY se.timestamp DESC")
    List<SecurityEvent> findRecentEvents(@Param("since") LocalDateTime since);
    
    @Query("SELECT se.ipAddress, COUNT(se) FROM SecurityEvent se WHERE se.timestamp >= :since GROUP BY se.ipAddress HAVING COUNT(se) >= :threshold ORDER BY COUNT(se) DESC")
    List<Object[]> findIpsWithHighEventCount(@Param("since") LocalDateTime since, @Param("threshold") Long threshold);
    
    @Query("SELECT COUNT(se) FROM SecurityEvent se WHERE se.severity = :severity AND se.isResolved = false")
    Long countUnresolvedEventsBySeverity(@Param("severity") String severity);
}
