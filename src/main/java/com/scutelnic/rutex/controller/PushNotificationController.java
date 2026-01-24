package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.PushSubscriptionRequest;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.PushSubscriptionService;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PushNotificationController {

    @Autowired
    private PushSubscriptionService pushSubscriptionService;

    @Value("${app.push.vapid.public-key:}")
    private String publicKey;

    @GetMapping("/api/push/vapid-public-key")
    public ResponseEntity<String> getPublicKey() {
        if (publicKey == null || publicKey.isBlank()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(publicKey);
    }

    @PostMapping("/api/push/subscribe")
    public ResponseEntity<Object> subscribe(@RequestBody PushSubscriptionRequest request, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        if (request == null || request.getEndpoint() == null || request.getEndpoint().isBlank()
            || request.getKeys() == null || request.getKeys().getP256dh() == null
            || request.getKeys().getAuth() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid subscription"));
        }

        pushSubscriptionService.saveSubscription(currentUser, request);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PostMapping("/api/push/unsubscribe")
    public ResponseEntity<Object> unsubscribe(@RequestBody Map<String, String> payload, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String endpoint = payload.getOrDefault("endpoint", "");
        boolean removed = pushSubscriptionService.removeSubscription(currentUser, endpoint);
        return ResponseEntity.ok(Map.of("success", removed));
    }
}
