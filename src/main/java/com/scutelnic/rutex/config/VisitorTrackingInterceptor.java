package com.scutelnic.rutex.config;

import com.scutelnic.rutex.service.SiteVisitorService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class VisitorTrackingInterceptor implements HandlerInterceptor {
    
    @Autowired
    private SiteVisitorService siteVisitorService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Skip tracking for admin pages and API calls to avoid inflating stats
        String requestURI = request.getRequestURI();
        
        if (shouldSkipTracking(requestURI)) {
            return true;
        }
        
        // Record the visit asynchronously to avoid blocking the request
        try {
            String ipAddress = getClientIpAddress(request);
            siteVisitorService.recordVisit(ipAddress, request);
        } catch (Exception e) {
            // Log error but don't break the request
            System.err.println("Error tracking visitor: " + e.getMessage());
        }
        
        return true;
    }
    
    /**
     * Determine if we should skip tracking for this request
     */
    private boolean shouldSkipTracking(String requestURI) {
        // Skip admin pages
        if (requestURI.startsWith("/admin")) {
            return true;
        }
        
        // Skip API calls
        if (requestURI.startsWith("/api")) {
            return true;
        }
        
        // Skip static resources
        if (requestURI.startsWith("/css/") || 
            requestURI.startsWith("/js/") || 
            requestURI.startsWith("/images/") || 
            requestURI.startsWith("/uploads/") ||
            requestURI.endsWith(".ico") ||
            requestURI.endsWith(".css") ||
            requestURI.endsWith(".js") ||
            requestURI.endsWith(".png") ||
            requestURI.endsWith(".jpg") ||
            requestURI.endsWith(".jpeg") ||
            requestURI.endsWith(".gif") ||
            requestURI.endsWith(".svg")) {
            return true;
        }
        
        // Skip favicon requests
        if (requestURI.contains("favicon")) {
            return true;
        }
        
        return false;
    }
    
    /**
     * Get client IP address from request
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
}
