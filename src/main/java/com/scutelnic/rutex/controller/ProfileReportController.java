package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.EmailService;
import com.scutelnic.rutex.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class ProfileReportController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserService userService;

    @Value("${app.base-url:}")
    private String baseUrl;

    @PostMapping("/reports/profile")
    public ResponseEntity<Map<String, Object>> submitProfileReport(@RequestBody ProfileReportRequest request) {
        Map<String, Object> response = new HashMap<>();

        if (request == null ||
            request.getReporterEmail() == null || request.getReporterEmail().trim().isEmpty() ||
            request.getDescription() == null || request.getDescription().trim().isEmpty() ||
            request.getProfileUserId() == null) {
            response.put("success", false);
            response.put("message", "Emailul, descrierea și profilul sunt obligatorii.");
            return ResponseEntity.badRequest().body(response);
        }

        Optional<User> userOpt = userService.getUserById(request.getProfileUserId());
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Profilul nu a fost găsit.");
            return ResponseEntity.status(404).body(response);
        }

        User user = userOpt.get();
        String subject = "Report profil - " + user.getFirstName() + " " + user.getLastName() + " (ID: " + user.getId() + ")";
        String profileUrl = resolveProfileUrl(request, user);
        String emailContent = buildEmailContent(request, user, profileUrl);

        emailService.sendEmailAsync("contact@rutex.md", subject, emailContent);

        response.put("success", true);
        response.put("message", "Raportul a fost trimis cu succes.");
        return ResponseEntity.ok(response);
    }

    private String buildEmailContent(ProfileReportRequest request, User user, String profileUrl) {
        String profileEmail = user.getEmail() != null ? user.getEmail() : "Nu specificat";
        String profilePhone = user.getPhone() != null ? user.getPhone() : "Nu specificat";
        String safeProfileUrl = profileUrl != null && !profileUrl.isBlank() ? profileUrl : "Nu specificat";

        return String.format("""
            <div class="header">
                <h1>RAPORT PROFIL - RUTEX</h1>
            </div>
            
            <div class="contact-info">
                <strong>Email raportor:</strong> %s<br>
                <strong>Profil raportat:</strong> %s %s (ID: %s)<br>
                <strong>Email profil:</strong> %s<br>
                <strong>Telefon profil:</strong> %s<br>
                <strong>Link profil:</strong> <a href="%s">%s</a>
            </div>
            
            <div class="highlight">
                <strong>Descriere:</strong><br>
                %s
            </div>
            
            <div class="footer">
                Acest raport a fost trimis prin formularul de raportare a profilului.
            </div>
            """,
            request.getReporterEmail().trim(),
            user.getFirstName(),
            user.getLastName(),
            user.getId(),
            profileEmail,
            profilePhone,
            safeProfileUrl,
            safeProfileUrl,
            request.getDescription().trim()
        );
    }

    private String resolveProfileUrl(ProfileReportRequest request, User user) {
        String trimmedBaseUrl = baseUrl != null ? baseUrl.trim() : "";
        if (request.getProfilePath() != null && !request.getProfilePath().trim().isEmpty()) {
            String path = request.getProfilePath().trim();
            if (!path.startsWith("/")) {
                path = "/" + path;
            }
            if (!trimmedBaseUrl.isEmpty()) {
                return trimTrailingSlash(trimmedBaseUrl) + path;
            }
            return path;
        }

        if (request.getProfileUrl() != null && !request.getProfileUrl().trim().isEmpty()) {
            return request.getProfileUrl().trim();
        }

        if (!trimmedBaseUrl.isEmpty()) {
            return trimTrailingSlash(trimmedBaseUrl) + "/ro/profile/" + user.getId();
        }

        return "";
    }

    private String trimTrailingSlash(String value) {
        if (value == null) {
            return "";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    public static class ProfileReportRequest {
        private String reporterEmail;
        private String description;
        private Long profileUserId;
        private String profileUrl;
        private String profilePath;

        public String getReporterEmail() {
            return reporterEmail;
        }

        public void setReporterEmail(String reporterEmail) {
            this.reporterEmail = reporterEmail;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Long getProfileUserId() {
            return profileUserId;
        }

        public void setProfileUserId(Long profileUserId) {
            this.profileUserId = profileUserId;
        }

        public String getProfileUrl() {
            return profileUrl;
        }

        public void setProfileUrl(String profileUrl) {
            this.profileUrl = profileUrl;
        }

        public String getProfilePath() {
            return profilePath;
        }

        public void setProfilePath(String profilePath) {
            this.profilePath = profilePath;
        }
    }
}
