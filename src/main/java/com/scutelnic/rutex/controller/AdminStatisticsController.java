package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.SiteVisitorService;
import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.service.UserService;
import com.scutelnic.rutex.service.LocalityService;
import com.scutelnic.rutex.service.GooglePlacesService;
import com.scutelnic.rutex.service.ContactActionService;
import com.scutelnic.rutex.entity.SiteVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminStatisticsController {
    
    @Autowired
    private SiteVisitorService siteVisitorService;
    
    @Autowired
    private RideService rideService;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private GooglePlacesService googlePlacesService;
    
    @Autowired
    private LocalityService localityService;

    @Autowired
    private ContactActionService contactActionService;
    
    /**
     * Admin dashboard with statistics
     */
    @GetMapping("/statistics")
    public String adminStatistics(Model model, HttpServletRequest request) {
        // Check if user is admin
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/ro/login";
        }
        
        // Get visitor statistics
        Map<String, Object> visitorStats = siteVisitorService.getStatistics();
        
        // Get ride statistics
        Map<String, Object> rideStats = new HashMap<>();
        rideStats.put("totalRides", rideService.getAllActiveRides().size());
        rideStats.put("activeRides", rideService.getAllActiveRides().size());
        
        // Get user statistics
        Map<String, Object> userStats = new HashMap<>();
        userStats.put("totalUsers", userService.getAllUsers().size());
        
        model.addAttribute("visitorStats", visitorStats);
        model.addAttribute("rideStats", rideStats);
        model.addAttribute("userStats", userStats);
        
        return "admin-statistics";
    }
    
    /**
     * API endpoint to get visitor statistics
     */
    @GetMapping("/api/statistics/visitors")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getVisitorStatistics() {
        Map<String, Object> stats = siteVisitorService.getStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * API endpoint to get Google Places API statistics
     */
    @GetMapping("/api/statistics/google-places")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getGooglePlacesStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Get Google Places API statistics
        Map<String, Object> apiStats = googlePlacesService.getApiStatistics();
        stats.putAll(apiStats);
        
        // Get locality search statistics (local vs Google API)
        Map<String, Object> searchStats = localityService.getSearchStatistics();
        stats.putAll(searchStats);
        
        return ResponseEntity.ok(stats);
    }

    /**
     * API endpoint to get contact action statistics
     */
    @GetMapping("/api/statistics/contact-actions")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getContactActionStatistics() {
        Map<String, Object> stats = contactActionService.getContactActionStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * API endpoint to get all visitors
     */
    @GetMapping("/api/visitors")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getAllVisitors(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("visitors", siteVisitorService.getAllActiveVisitors());
        response.put("totalVisitors", siteVisitorService.getTotalVisitors());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to get top visitors
     */
    @GetMapping("/api/visitors/top")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getTopVisitors(
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        response.put("topVisitors", siteVisitorService.getTopVisitors(limit));
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to get recent visitors
     */
    @GetMapping("/api/visitors/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentVisitors(
            @RequestParam(defaultValue = "30") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        List<SiteVisitor> recentVisitors = siteVisitorService.getRecentVisitors(limit);
        response.put("recentVisitors", recentVisitors);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to deactivate a visitor
     */
    @PostMapping("/api/visitors/{visitorId}/deactivate")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> deactivateVisitor(@PathVariable Long visitorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            siteVisitorService.deactivateVisitor(visitorId);
            response.put("success", true);
            response.put("message", "Vizitatorul a fost dezactivat cu succes");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la dezactivarea vizitatorului: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to ban a visitor (block their IP)
     */
    @PostMapping("/api/visitors/{visitorId}/ban")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> banVisitor(@PathVariable Long visitorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            siteVisitorService.banVisitor(visitorId);
            response.put("success", true);
            response.put("message", "IP-ul a fost banat cu succes");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la banarea IP-ului: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to unban a visitor (unblock their IP)
     */
    @PostMapping("/api/visitors/{visitorId}/unban")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> unbanVisitor(@PathVariable Long visitorId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            siteVisitorService.unbanVisitor(visitorId);
            response.put("success", true);
            response.put("message", "IP-ul a fost debanat cu succes");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la debanarea IP-ului: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to get banned visitors
     */
    @GetMapping("/api/visitors/banned")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getBannedVisitors() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<SiteVisitor> bannedVisitors = siteVisitorService.getBannedVisitors();
            Long bannedCount = siteVisitorService.getBannedVisitorsCount();
            
            response.put("success", true);
            response.put("bannedVisitors", bannedVisitors);
            response.put("bannedCount", bannedCount);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la obținerea listei de IP-uri banate: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to record a visit (called from interceptor)
     */
    @PostMapping("/api/visitors/record")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> recordVisit(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String ipAddress = getClientIpAddress(request);
            siteVisitorService.recordVisit(ipAddress, request);
            response.put("success", true);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error recording visit: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
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
