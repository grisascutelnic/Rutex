package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.RouteSeoStatisticsService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class RouteSeoStatisticsController {

    private final RouteSeoStatisticsService routeSeoStatisticsService;

    public RouteSeoStatisticsController(RouteSeoStatisticsService routeSeoStatisticsService) {
        this.routeSeoStatisticsService = routeSeoStatisticsService;
    }

    @PostMapping("/api/route-seo/events")
    public ResponseEntity<Map<String, Object>> recordRouteSeoEvent(@RequestBody Map<String, Object> requestBody,
                                                                   HttpServletRequest request,
                                                                   HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        Long userId = currentUser != null ? currentUser.getId() : null;
        routeSeoStatisticsService.recordEvent(
                stringValue(requestBody.get("routeSlug")),
                stringValue(requestBody.get("language")),
                stringValue(requestBody.get("eventType")),
                stringValue(requestBody.get("visitorKey")),
                userId,
                longValue(requestBody.get("rideId")),
                stringValue(requestBody.get("pageUrl")),
                stringValue(requestBody.get("referrer")),
                request
        );

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/api/statistics/seo-routes")
    public ResponseEntity<Map<String, Object>> getRouteSeoStatistics() {
        return ResponseEntity.ok(routeSeoStatisticsService.getAdminStatistics());
    }

    @PostMapping("/admin/api/statistics/seo-routes/verified")
    public ResponseEntity<Map<String, Object>> updateRouteSeoVerified(@RequestBody Map<String, Object> requestBody,
                                                                      HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRoles() == null
                || currentUser.getRoles().stream().noneMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {
            return ResponseEntity.status(403).body(Map.of("success", false));
        }

        Long pageId = longValue(requestBody.get("id"));
        boolean adminVerified = Boolean.parseBoolean(stringValue(requestBody.get("adminVerified")));
        if (pageId == null || !routeSeoStatisticsService.updateAdminVerified(pageId, adminVerified)) {
            return ResponseEntity.status(404).body(Map.of("success", false));
        }
        return ResponseEntity.ok(Map.of("success", true, "adminVerified", adminVerified));
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private Long longValue(Object value) {
        if (value == null || value.toString().isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
