package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.SecurityEvent;
import com.scutelnic.rutex.repository.SecurityEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Service
@Transactional
public class SecurityMonitoringService {
    
    @Autowired
    private SecurityEventRepository securityEventRepository;
    
    // In-memory cache for rate limiting
    private final Map<String, RequestCounter> requestCounters = new ConcurrentHashMap<>();

    // Debounce security event writes to reduce DB load under bot traffic
    private final Map<String, Long> lastEventWriteMs = new ConcurrentHashMap<>();
    private static final long EVENT_WRITE_DEBOUNCE_MS = 60_000L;
    
    // Suspicious User-Agent patterns
    private static final List<Pattern> SUSPICIOUS_USER_AGENT_PATTERNS = List.of(
        Pattern.compile("(?i)bot|crawler|spider|scraper"),
        Pattern.compile("(?i)curl|wget|python|java|perl"),
        Pattern.compile("(?i)sqlmap|nikto|nmap|metasploit"),
        Pattern.compile("(?i)masscan|zmap|scanner"),
        Pattern.compile("(?i)headless|phantom|selenium"),
        Pattern.compile("(?i)admin|hack|exploit|inject")
    );
    
    // Rate limiting thresholds
    private static final int RATE_LIMIT_THRESHOLD = 50; // requests per minute
    private static final int RATE_LIMIT_WINDOW = 60; // seconds
    
    /**
     * Monitor a request for security threats
     */
    public void monitorRequest(HttpServletRequest request) {
        String ipAddress = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        String requestUrl = request.getRequestURI();
        String requestMethod = request.getMethod();
        
        // Check for suspicious User-Agent
        if (isSuspiciousUserAgent(userAgent)) {
            recordSecurityEvent(ipAddress, "SUSPICIOUS_USER_AGENT", 
                "Suspicious User-Agent detected: " + userAgent, 
                userAgent, requestUrl, requestMethod, "MEDIUM");
        }
        
        // Check rate limiting
        if (isRateLimitExceeded(ipAddress)) {
            recordSecurityEvent(ipAddress, "RATE_LIMIT_EXCEEDED", 
                "Rate limit exceeded: " + getRequestCount(ipAddress) + " requests in " + RATE_LIMIT_WINDOW + " seconds", 
                userAgent, requestUrl, requestMethod, "HIGH");
        }
        
        // Update request counter
        updateRequestCounter(ipAddress);
    }
    
    /**
     * Check if User-Agent is suspicious
     */
    private boolean isSuspiciousUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return true; // Empty User-Agent is suspicious
        }
        
        return SUSPICIOUS_USER_AGENT_PATTERNS.stream()
            .anyMatch(pattern -> pattern.matcher(userAgent).find());
    }
    
    /**
     * Check if rate limit is exceeded
     */
    private boolean isRateLimitExceeded(String ipAddress) {
        RequestCounter counter = requestCounters.get(ipAddress);
        if (counter == null) {
            return false;
        }
        
        // Clean old entries
        counter.cleanOldEntries();
        
        return counter.getCount() > RATE_LIMIT_THRESHOLD;
    }
    
    /**
     * Get current request count for IP
     */
    private int getRequestCount(String ipAddress) {
        RequestCounter counter = requestCounters.get(ipAddress);
        if (counter == null) {
            return 0;
        }
        counter.cleanOldEntries();
        return counter.getCount();
    }
    
    /**
     * Update request counter for IP
     */
    private void updateRequestCounter(String ipAddress) {
        requestCounters.computeIfAbsent(ipAddress, k -> new RequestCounter())
                      .addRequest();
    }
    
    /**
     * Record a security event
     */
    public void recordSecurityEvent(String ipAddress, String eventType, String description, 
                                  String userAgent, String requestUrl, String requestMethod, String severity) {
        long nowMs = System.currentTimeMillis();
        String key = ipAddress + "|" + eventType;
        Long lastWrite = lastEventWriteMs.get(key);
        if (lastWrite != null && (nowMs - lastWrite) < EVENT_WRITE_DEBOUNCE_MS) {
            return;
        }

        SecurityEvent event = new SecurityEvent();
        event.setIpAddress(ipAddress);
        event.setEventType(eventType);
        event.setDescription(description);
        event.setUserAgent(userAgent);
        event.setRequestUrl(requestUrl);
        event.setRequestMethod(requestMethod);
        event.setTimestamp(LocalDateTime.now());
        event.setSeverity(severity);
        event.setRequestCount(getRequestCount(ipAddress));
        
        securityEventRepository.save(event);
        lastEventWriteMs.put(key, nowMs);
    }
    

    
    /**
     * Get recent security events
     */
    public List<SecurityEvent> getRecentEvents(int hours) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        return securityEventRepository.findRecentEvents(since);
    }
    
    /**
     * Get unresolved security events
     */
    public List<SecurityEvent> getUnresolvedEvents() {
        return securityEventRepository.findByIsResolvedFalseOrderByTimestampDesc();
    }
    
    /**
     * Mark event as resolved
     */
    public void resolveEvent(Long eventId) {
        securityEventRepository.findById(eventId).ifPresent(event -> {
            event.setIsResolved(true);
            securityEventRepository.save(event);
        });
    }
    
    /**
     * Get security statistics
     */
    public Map<String, Object> getSecurityStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        stats.put("eventsLast24Hours", securityEventRepository.countEventsByIpSince("", last24Hours));
        stats.put("unresolvedCritical", securityEventRepository.countUnresolvedEventsBySeverity("CRITICAL"));
        stats.put("unresolvedHigh", securityEventRepository.countUnresolvedEventsBySeverity("HIGH"));
        stats.put("unresolvedMedium", securityEventRepository.countUnresolvedEventsBySeverity("MEDIUM"));
        
        return stats;
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
    
    /**
     * Inner class for request counting
     */
    private static class RequestCounter {
        private final List<Long> requestTimes = new java.util.ArrayList<>();
        
        public synchronized void addRequest() {
            requestTimes.add(System.currentTimeMillis());
        }
        
        public synchronized int getCount() {
            return requestTimes.size();
        }
        
        public synchronized void cleanOldEntries() {
            long cutoff = System.currentTimeMillis() - (RATE_LIMIT_WINDOW * 1000L);
            requestTimes.removeIf(time -> time < cutoff);
        }
    }
}
