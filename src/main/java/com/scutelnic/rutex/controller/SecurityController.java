package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.SecurityEvent;
import com.scutelnic.rutex.service.SecurityMonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/admin/security")
public class SecurityController {
    
    @Autowired
    private SecurityMonitoringService securityMonitoringService;
    
    /**
     * Security dashboard page
     */
    @GetMapping("/dashboard")
    public String securityDashboard(Model model) {
        Map<String, Object> stats = securityMonitoringService.getSecurityStatistics();
        List<SecurityEvent> recentEvents = securityMonitoringService.getRecentEvents(24);
        List<SecurityEvent> unresolvedEvents = securityMonitoringService.getUnresolvedEvents();
        
        model.addAttribute("securityStats", stats);
        model.addAttribute("recentEvents", recentEvents);
        model.addAttribute("unresolvedEvents", unresolvedEvents);
        
        return "security-dashboard";
    }
    
    /**
     * API endpoint to get security statistics
     */
    @GetMapping("/api/statistics")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getSecurityStatistics() {
        Map<String, Object> stats = securityMonitoringService.getSecurityStatistics();
        return ResponseEntity.ok(stats);
    }
    
    /**
     * API endpoint to get recent security events
     */
    @GetMapping("/api/events/recent")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getRecentEvents(@RequestParam(defaultValue = "24") int hours) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<SecurityEvent> events = securityMonitoringService.getRecentEvents(hours);
            response.put("success", true);
            response.put("events", events);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting recent events: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to get unresolved security events
     */
    @GetMapping("/api/events/unresolved")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getUnresolvedEvents() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<SecurityEvent> events = securityMonitoringService.getUnresolvedEvents();
            response.put("success", true);
            response.put("events", events);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error getting unresolved events: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * API endpoint to resolve a security event
     */
    @PostMapping("/api/events/{eventId}/resolve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> resolveEvent(@PathVariable Long eventId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            securityMonitoringService.resolveEvent(eventId);
            response.put("success", true);
            response.put("message", "Event resolved successfully");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Error resolving event: " + e.getMessage());
        }
        
        return ResponseEntity.ok(response);
    }
}
