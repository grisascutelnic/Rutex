package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.SiteVisitor;
import com.scutelnic.rutex.repository.SiteVisitorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Transactional
public class SiteVisitorService {
    
    @Autowired
    private SiteVisitorRepository siteVisitorRepository;

    // Cache banned status to avoid DB hit on every request
    private final Map<String, CacheEntry> bannedIpCache = new ConcurrentHashMap<>();
    private static final long BANNED_CACHE_TTL_MS = 60_000L;

    // Throttle visitor updates per IP to reduce DB load
    private final Map<String, Long> lastVisitWriteMs = new ConcurrentHashMap<>();
    private static final long VISIT_WRITE_THROTTLE_MS = 60_000L;
    
    /**
     * Record a site visit for the given IP address
     */
    public void recordVisit(String ipAddress, HttpServletRequest request) {
        try {
            Long lastWrite = lastVisitWriteMs.get(ipAddress);
            long nowMs = System.currentTimeMillis();
            if (lastWrite != null && (nowMs - lastWrite) < VISIT_WRITE_THROTTLE_MS) {
                return;
            }

            Optional<SiteVisitor> existingVisitor = siteVisitorRepository.findByIpAddress(ipAddress);
            
            if (existingVisitor.isPresent()) {
                // Update existing visitor
                SiteVisitor visitor = existingVisitor.get();
                visitor.setLastVisit(LocalDateTime.now());
                visitor.setVisitCount(visitor.getVisitCount() + 1);
                visitor.setUserAgent(request.getHeader("User-Agent"));
                siteVisitorRepository.save(visitor);
            } else {
                // Create new visitor
                SiteVisitor newVisitor = new SiteVisitor();
                newVisitor.setIpAddress(ipAddress);
                newVisitor.setFirstVisit(LocalDateTime.now());
                newVisitor.setLastVisit(LocalDateTime.now());
                newVisitor.setUserAgent(request.getHeader("User-Agent"));
                newVisitor.setVisitCount(1);
                newVisitor.setIsActive(true);
                
                // Try to get location info (optional)
                try {
                    // You can integrate with a geolocation service here
                    // For now, we'll leave it null
                } catch (Exception e) {
                    // Ignore geolocation errors
                }
                
                siteVisitorRepository.save(newVisitor);
            }
            lastVisitWriteMs.put(ipAddress, nowMs);
        } catch (Exception e) {
            // Log error but don't break the application
            System.err.println("Error recording site visit: " + e.getMessage());
        }
    }
    
    /**
     * Get total number of unique visitors
     */
    public Long getTotalVisitors() {
        return siteVisitorRepository.countActiveVisitors();
    }
    
    /**
     * Get visitors count since a specific date
     */
    public Long getVisitorsSince(LocalDateTime since) {
        return siteVisitorRepository.countVisitorsSince(since);
    }
    
    /**
     * Get visitors count for today
     */
    public Long getTodayVisitors() {
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        return siteVisitorRepository.countVisitorsSince(today);
    }
    
    /**
     * Get visitors count for this week
     */
    public Long getThisWeekVisitors() {
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        return siteVisitorRepository.countVisitorsSince(weekAgo);
    }
    
    /**
     * Get visitors count for this month
     */
    public Long getThisMonthVisitors() {
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        return siteVisitorRepository.countVisitorsSince(monthAgo);
    }
    
    /**
     * Get all active visitors ordered by last visit
     */
    public List<SiteVisitor> getAllActiveVisitors() {
        return siteVisitorRepository.findAllActiveVisitorsOrderByLastVisit();
    }
    
    /**
     * Get top visitors by visit count
     */
    public List<SiteVisitor> getTopVisitors(int limit) {
        List<SiteVisitor> allVisitors = siteVisitorRepository.findAllActiveVisitorsOrderByVisitCount();
        return allVisitors.subList(0, Math.min(limit, allVisitors.size()));
    }
    
    /**
     * Get recent visitors (limited to 70 most recent by ID)
     */
    public List<SiteVisitor> getRecentVisitors(int limit) {
        List<SiteVisitor> allVisitors = siteVisitorRepository.findAllActiveVisitorsOrderByIdDesc();
        
        // Dacă avem mai mult de 70 de vizitatori, luăm doar primii 70 (cele cu ID-urile cele mai mari)
        if (allVisitors.size() > 70) {
            allVisitors = allVisitors.subList(0, 70);
        }
        
        // Apoi aplicăm limita cerută
        int actualLimit = Math.min(limit, allVisitors.size());
        return allVisitors.subList(0, actualLimit);
    }
    
    /**
     * Get visitors by country
     */
    public Map<String, Long> getVisitorsByCountry() {
        List<Object[]> results = siteVisitorRepository.getVisitorsByCountry();
        Map<String, Long> countryStats = new HashMap<>();
        
        for (Object[] result : results) {
            String country = (String) result[0];
            Long count = (Long) result[1];
            countryStats.put(country != null ? country : "Unknown", count);
        }
        
        return countryStats;
    }
    
    /**
     * Get comprehensive statistics
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("totalVisitors", getTotalVisitors());
        stats.put("todayVisitors", getTodayVisitors());
        stats.put("thisWeekVisitors", getThisWeekVisitors());
        stats.put("thisMonthVisitors", getThisMonthVisitors());
        stats.put("visitorsByCountry", getVisitorsByCountry());
        stats.put("topVisitors", getTopVisitors(10));
        stats.put("recentVisitors", getRecentVisitors(10));
        
        return stats;
    }
    
    /**
     * Get visitor by IP address
     */
    public Optional<SiteVisitor> getVisitorByIp(String ipAddress) {
        return siteVisitorRepository.findByIpAddress(ipAddress);
    }
    
    /**
     * Deactivate a visitor (soft delete)
     */
    public void deactivateVisitor(Long visitorId) {
        Optional<SiteVisitor> visitor = siteVisitorRepository.findById(visitorId);
        if (visitor.isPresent()) {
            SiteVisitor v = visitor.get();
            v.setIsActive(false);
            siteVisitorRepository.save(v);
        }
    }
    
    /**
     * Ban a visitor (block their IP)
     */
    public void banVisitor(Long visitorId) {
        Optional<SiteVisitor> visitor = siteVisitorRepository.findById(visitorId);
        if (visitor.isPresent()) {
            SiteVisitor v = visitor.get();
            v.setBanned(true);
            siteVisitorRepository.save(v);
        }
    }
    
    /**
     * Unban a visitor (unblock their IP)
     */
    public void unbanVisitor(Long visitorId) {
        Optional<SiteVisitor> visitor = siteVisitorRepository.findById(visitorId);
        if (visitor.isPresent()) {
            SiteVisitor v = visitor.get();
            v.setBanned(false);
            siteVisitorRepository.save(v);
        }
    }
    
    /**
     * Check if an IP is banned
     */
    public boolean isIpBanned(String ipAddress) {
        CacheEntry cached = bannedIpCache.get(ipAddress);
        long nowMs = System.currentTimeMillis();
        if (cached != null && (nowMs - cached.timestampMs) < BANNED_CACHE_TTL_MS) {
            return cached.isBanned;
        }

        boolean isBanned = siteVisitorRepository.findByIpAddressAndBannedTrue(ipAddress).isPresent();
        bannedIpCache.put(ipAddress, new CacheEntry(isBanned, nowMs));
        return isBanned;
    }
    
    /**
     * Get all banned visitors
     */
    public List<SiteVisitor> getBannedVisitors() {
        return siteVisitorRepository.findByBannedTrueOrderByLastVisitDesc();
    }
    
    /**
     * Get count of banned visitors
     */
    public Long getBannedVisitorsCount() {
        return siteVisitorRepository.countBannedVisitors();
    }

    private static class CacheEntry {
        private final boolean isBanned;
        private final long timestampMs;

        private CacheEntry(boolean isBanned, long timestampMs) {
            this.isBanned = isBanned;
            this.timestampMs = timestampMs;
        }
    }
}
