package com.scutelnic.rutex.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.scutelnic.rutex.service.EmailService;
import com.scutelnic.rutex.service.RecaptchaService;
import com.scutelnic.rutex.service.SecurityMonitoringService;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class ContactController {

    @Autowired
    private EmailService emailService;
    
    @Autowired
    private RecaptchaService recaptchaService;
    
    @Autowired
    private SecurityMonitoringService securityMonitoringService;

    @GetMapping("/contact/test")
    public ResponseEntity<Map<String, Object>> testContact() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contact controller is working!");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContactForm(@RequestBody ContactFormRequest request, HttpServletRequest httpRequest) {
        Map<String, Object> response = new HashMap<>();
        
        System.out.println("=== CONTACT FORM RECEIVED ===");
        System.out.println("Request: " + request);
        
        // Monitor request for security threats
        securityMonitoringService.monitorRequest(httpRequest);
        
        try {
            // Verify reCAPTCHA
            if (!recaptchaService.verifyRecaptcha(request.getRecaptchaResponse(), getClientIpAddress(httpRequest))) {
                response.put("success", false);
                response.put("message", "Verificarea reCAPTCHA a eșuat. Vă rugăm să încercați din nou.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // Validate request
            if (request.getFirstName() == null || request.getFirstName().trim().isEmpty() ||
                request.getLastName() == null || request.getLastName().trim().isEmpty() ||
                request.getEmail() == null || request.getEmail().trim().isEmpty() ||
                request.getSubject() == null || request.getSubject().trim().isEmpty() ||
                request.getMessage() == null || request.getMessage().trim().isEmpty()) {
                
                System.out.println("Validation failed - missing fields");
                response.put("success", false);
                response.put("message", "Toate câmpurile sunt obligatorii");
                return ResponseEntity.badRequest().body(response);
            }

            // Send emails async to avoid blocking the response
            String emailContent = buildEmailContent(request);
            System.out.println("=== QUEUING EMAIL TO contact@rutex.md ===");
            emailService.sendEmailAsync("contact@rutex.md", "Mesaj nou de contact - " + request.getSubject(), emailContent);
            
            // Send confirmation email to user
            String confirmationContent = buildConfirmationEmail(request);
            System.out.println("=== QUEUING CONFIRMATION EMAIL TO " + request.getEmail() + " ===");
            emailService.sendEmailAsync(request.getEmail(), "Confirmare mesaj contact - Rutex", confirmationContent);

            response.put("success", true);
            response.put("message", "Mesajul a fost trimis cu succes");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.err.println("=== CONTACT FORM ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "A apărut o eroare la trimiterea mesajului: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    private String buildEmailContent(ContactFormRequest request) {
        return String.format("""
            <div class="header">
                <h1>MESAJ NOU DE CONTACT - RUTEX</h1>
            </div>
            
            <div class="contact-info">
                <strong>Nume:</strong> %s %s<br>
                <strong>Email:</strong> %s<br>
                <strong>Subiect:</strong> %s
            </div>
            
            <div class="highlight">
                <strong>Mesaj:</strong><br>
                %s
            </div>
            
            <div class="footer">
                Acest mesaj a fost trimis prin formularul de contact de pe site-ul Rutex.
            </div>
            """, 
            request.getFirstName(), 
            request.getLastName(), 
            request.getEmail(), 
            request.getSubject(), 
            request.getMessage()
        );
    }

    private String buildConfirmationEmail(ContactFormRequest request) {
        return String.format("""
            <div class="header">
                <h1>Confirmare Mesaj Contact</h1>
            </div>
            
            <div class="contact-info">
                <strong>Nume:</strong> %s %s<br>
                <strong>Email:</strong> %s<br>
                <strong>Subiect:</strong> %s
            </div>
            
            <div class="highlight">
                <strong>Mesajul trimis:</strong><br>
                %s
            </div>
            
            <p>Echipa noastră va analiza mesajul și vă va răspunde în cel mai scurt timp posibil.</p>
            
            <p><strong>Cu stimă,</strong><br>
            <strong>Echipa Rutex</strong></p>
            
            <div class="footer">
                Acest mesaj a fost generat automat de sistemul Rutex.
            </div>
            """, 
            request.getFirstName(), 
            request.getLastName(), 
            request.getEmail(),
            request.getSubject(), 
            request.getMessage()
        );
    }

    // Inner class for request mapping
    public static class ContactFormRequest {
        private String firstName;
        private String lastName;
        private String email;
        private String subject;
        private String message;
        private String recaptchaResponse;

        // Getters and setters
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public String getSubject() { return subject; }
        public void setSubject(String subject) { this.subject = subject; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getRecaptchaResponse() { return recaptchaResponse; }
        public void setRecaptchaResponse(String recaptchaResponse) { this.recaptchaResponse = recaptchaResponse; }
    }
    
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
