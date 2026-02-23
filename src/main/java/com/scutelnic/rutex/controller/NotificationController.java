package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.Notification;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.NotificationService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping("/api/notifications")
    public ResponseEntity<Object> getNotifications(
        @RequestParam(value = "limit", defaultValue = "10") int limit,
        @RequestParam(value = "lang", defaultValue = "ro") String lang,
        HttpSession session) {

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        List<Notification> notifications = notificationService.getNotifications(currentUser.getId(), limit);
        List<Map<String, Object>> payload = notifications.stream()
            .map(notification -> {
                Map<String, Object> entry = new HashMap<>();
                entry.put("id", notification.getId());
                entry.put("title", "ru".equals(lang) ? notification.getTitleRu() : notification.getTitleRo());
                entry.put("message", "ru".equals(lang) ? notification.getMessageRu() : notification.getMessageRo());
                entry.put("createdAt", notification.getCreatedAt() != null ? notification.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                entry.put("readAt", notification.getReadAt() != null ? notification.getReadAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) : null);
                return entry;
            })
            .collect(Collectors.toList());

        return ResponseEntity.ok(payload);
    }

    @GetMapping("/api/notifications/unread-count")
    public ResponseEntity<Object> getUnreadCount(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        long count = notificationService.getUnreadCount(currentUser.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/api/notifications/{id}/read")
    public ResponseEntity<Object> markRead(@PathVariable Long id, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        boolean updated = notificationService.markRead(currentUser.getId(), id);
        return ResponseEntity.ok(Map.of("success", updated));
    }

    @PostMapping("/api/notifications/read-all")
    public ResponseEntity<Object> markAllRead(HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        int updated = notificationService.markAllRead(currentUser.getId());
        return ResponseEntity.ok(Map.of("updated", updated));
    }

    @PostMapping("/admin/api/notifications/broadcast")
    public ResponseEntity<Object> broadcast(@RequestBody Map<String, String> payload, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRoles() == null ||
            currentUser.getRoles().stream().noneMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
        }

        String titleRo = payload.getOrDefault("titleRo", "").trim();
        String messageRo = payload.getOrDefault("messageRo", "").trim();
        String titleRu = payload.getOrDefault("titleRu", "").trim();
        String messageRu = payload.getOrDefault("messageRu", "").trim();

        if (titleRo.isEmpty() || messageRo.isEmpty() || titleRu.isEmpty() || messageRu.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        int sent = notificationService.broadcastToAll(titleRo, messageRo, titleRu, messageRu);
        return ResponseEntity.ok(Map.of("sent", sent));
    }

    @PostMapping("/admin/api/notifications/send-to-user")
    public ResponseEntity<Object> sendToUser(@RequestBody Map<String, String> payload, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRoles() == null ||
            currentUser.getRoles().stream().noneMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
        }

        String email = payload.getOrDefault("email", "").trim();
        String titleRo = payload.getOrDefault("titleRo", "").trim();
        String messageRo = payload.getOrDefault("messageRo", "").trim();
        String titleRu = payload.getOrDefault("titleRu", "").trim();
        String messageRu = payload.getOrDefault("messageRu", "").trim();

        if (email.isEmpty() || titleRo.isEmpty() || messageRo.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        if (titleRu.isEmpty()) {
            titleRu = titleRo;
        }
        if (messageRu.isEmpty()) {
            messageRu = messageRo;
        }

        boolean sent = notificationService.sendToUserByEmail(email, titleRo, messageRo, titleRu, messageRu);
        if (!sent) {
            return ResponseEntity.status(404).body(Map.of("error", "User not found"));
        }

        return ResponseEntity.ok(Map.of("sent", 1));
    }

    @PostMapping("/admin/api/notifications/send-email-by-id-range")
    public ResponseEntity<Object> sendEmailByIdRange(@RequestBody Map<String, String> payload, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getRoles() == null ||
            currentUser.getRoles().stream().noneMatch(role -> "ROLE_ADMIN".equals(role.getName()))) {
            return ResponseEntity.status(403).body(Map.of("error", "Not authorized"));
        }

        String startIdRaw = payload.getOrDefault("startId", "").trim();
        String endIdRaw = payload.getOrDefault("endId", "").trim();
        String titleRo = payload.getOrDefault("titleRo", "").trim();
        String messageRo = payload.getOrDefault("messageRo", "").trim();
        String titleRu = payload.getOrDefault("titleRu", "").trim();
        String messageRu = payload.getOrDefault("messageRu", "").trim();

        if (startIdRaw.isEmpty() || endIdRaw.isEmpty() || titleRo.isEmpty() || messageRo.isEmpty() || titleRu.isEmpty() || messageRu.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing fields"));
        }

        Long startId;
        Long endId;
        try {
            startId = Long.parseLong(startIdRaw);
            endId = Long.parseLong(endIdRaw);
        } catch (NumberFormatException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID-urile trebuie să fie numere valide"));
        }

        if (startId <= 0 || endId <= 0) {
            return ResponseEntity.badRequest().body(Map.of("error", "ID-urile trebuie să fie mai mari ca 0"));
        }

        Map<String, Integer> result = notificationService.sendEmailByUserIdRange(startId, endId, titleRo, messageRo, titleRu, messageRu);
        return ResponseEntity.ok(result);
    }
}
