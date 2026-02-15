package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.UserService;
import com.scutelnic.rutex.service.PasswordResetService;
import com.scutelnic.rutex.service.CloudinaryService;
import com.scutelnic.rutex.service.RecaptchaService;
import com.scutelnic.rutex.service.SecurityMonitoringService;
import com.scutelnic.rutex.dto.LoginRequest;
import com.scutelnic.rutex.dto.RegisterRequest;
import com.scutelnic.rutex.dto.AuthResponse;
import com.scutelnic.rutex.dto.ForgotPasswordRequest;
import com.scutelnic.rutex.dto.ResetPasswordRequest;
import com.scutelnic.rutex.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import com.scutelnic.rutex.dto.LoginResponseDTO;
import com.scutelnic.rutex.dto.RegisterResponseDTO;
import org.springframework.web.bind.annotation.*;


import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private CloudinaryService cloudinaryService; // Reserved for future use (profile image handling)
    
    @Autowired
    private RecaptchaService recaptchaService;
    
    @Autowired
    private SecurityMonitoringService securityMonitoringService;
    
    @Value("${app.session.remember-me.timeout:30d}")
    private String rememberMeTimeout;

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequest loginRequest, HttpSession session, HttpServletRequest request) {
        // Login attempt processing
        
        AuthResponse response = userService.login(loginRequest);
        // Login response processed
        
        if (response.isSuccess()) {
            // Store user in session
            session.setAttribute("user", response.getUser());
            // User stored in session

            if (!userService.hasPhoneNumber(response.getUser())) {
                session.setAttribute("forcePhoneCompletion", true);
            } else {
                session.removeAttribute("forcePhoneCompletion");
            }
            
            int rememberMeSeconds = parseTimeoutToSeconds(rememberMeTimeout);
            session.setMaxInactiveInterval(rememberMeSeconds);
            System.out.println("🔐 Persistent session enabled - timeout set to: " + rememberMeSeconds + " seconds (" + (rememberMeSeconds / 86400) + " days)");
            
            // Verify the timeout was set correctly
            int actualTimeout = session.getMaxInactiveInterval();
            System.out.println("✅ Actual session timeout after setting: " + actualTimeout + " seconds");
            
            // Login successful
            
            // Return a simple response instead of AuthResponse with User
            int sessionTimeout = session.getMaxInactiveInterval();
            System.out.println("📤 Login response - Session timeout: " + sessionTimeout + " seconds, Remember Me: true");

            LoginResponseDTO dto = new LoginResponseDTO(
                true,
                response.getMessage(),
                response.getUser().getEmail(),
                response.getUser().getId(),
                sessionTimeout,
                true
            );
            return ResponseEntity.ok(dto);
        } else {
                    // Login failed
            return ResponseEntity.badRequest().body(new LoginResponseDTO(false, response.getMessage(), null, null, null, null));
        }
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
    
    /**
     * Convertește un string de timeout (ex: "30m", "2h", "7d") în secunde
     */
    private int parseTimeoutToSeconds(String timeout) {
        if (timeout == null || timeout.isEmpty()) {
            System.out.println("⚠️ Timeout is null or empty, using default 30 minutes");
            return 30 * 60; // 30 minute default
        }
        
        String unit = timeout.substring(timeout.length() - 1).toLowerCase();
        int value = Integer.parseInt(timeout.substring(0, timeout.length() - 1));
        
        int result;
        switch (unit) {
            case "s": result = value; break;
            case "m": result = value * 60; break;
            case "h": result = value * 60 * 60; break;
            case "d": result = value * 24 * 60 * 60; break;
            default: 
                System.out.println("⚠️ Unknown timeout unit: " + unit + ", using default 30 minutes");
                result = 30 * 60; // 30 minute default
                break;
        }
        
        System.out.println("⏱️ Parsed timeout: " + timeout + " -> " + result + " seconds");
        return result;
    }

    @PostMapping("/register")
    public ResponseEntity<Object> register(
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "password", required = false) String password,
            @RequestParam(value = "firstName", required = false) String firstName,
            @RequestParam(value = "lastName", required = false) String lastName,
            @RequestParam(value = "recaptchaResponse", required = false) String recaptchaResponse,
            HttpSession session,
            HttpServletRequest request) {
        
        // Monitor request for security threats
        securityMonitoringService.monitorRequest(request);
        
        // Verify reCAPTCHA
        if (!recaptchaService.verifyRecaptcha(recaptchaResponse, getClientIpAddress(request))) {
            return ResponseEntity.badRequest().body(new RegisterResponseDTO(false, "Verificarea reCAPTCHA a eșuat. Vă rugăm să încercați din nou.", null, null));
        }
        
        // Validare manuală a parametrilor
        if (email == null || email.trim().isEmpty()) {
            System.err.println("ERROR: Email is null or empty");
            return ResponseEntity.badRequest().body(new RegisterResponseDTO(false, "Email-ul este obligatoriu", null, null));
        }
        
        if (password == null || password.trim().isEmpty()) {
            System.err.println("ERROR: Password is null or empty");
            return ResponseEntity.badRequest().body(new RegisterResponseDTO(false, "Parola este obligatorie", null, null));
        }
        
        if (firstName == null || firstName.trim().isEmpty()) {
            System.err.println("ERROR: FirstName is null or empty");
            return ResponseEntity.badRequest().body(new RegisterResponseDTO(false, "Prenumele este obligatoriu", null, null));
        }
        
        if (lastName == null || lastName.trim().isEmpty()) {
            System.err.println("ERROR: LastName is null or empty");
            return ResponseEntity.badRequest().body(new RegisterResponseDTO(false, "Numele este obligatoriu", null, null));
        }
        
        System.out.println("Creating RegisterRequest object...");
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail(email);
        registerRequest.setPassword(password);
        registerRequest.setFirstName(firstName);
        registerRequest.setLastName(lastName);
        registerRequest.setPhone("");
        registerRequest.setPhonePrefix(null);
        System.out.println("RegisterRequest created with email: " + registerRequest.getEmail());
        System.out.println("RegisterRequest phonePrefix: " + registerRequest.getPhonePrefix());
        
        System.out.println("Calling userService.register...");
        AuthResponse response;
        try {
            response = userService.register(registerRequest);
            System.out.println("Registration response: " + response.isSuccess() + " - " + response.getMessage());
        } catch (Exception e) {
            System.err.println("=== REGISTRATION EXCEPTION ===");
            System.err.println("Exception type: " + e.getClass().getName());
            System.err.println("Exception message: " + e.getMessage());
            e.printStackTrace();
            
            return ResponseEntity.badRequest().body(new RegisterResponseDTO(false, "Eroare internă: " + e.getMessage(), null, null));
        }
        
        if (response.isSuccess()) {
            // Store user in session after registration
            session.setAttribute("user", response.getUser());
            System.out.println("User stored in session: " + response.getUser().getEmail());

            int rememberMeSeconds = parseTimeoutToSeconds(rememberMeTimeout);
            session.setMaxInactiveInterval(rememberMeSeconds);
            System.out.println("🔐 Registration persistent session enabled - timeout set to: " + rememberMeSeconds + " seconds (" + (rememberMeSeconds / 86400) + " days)");

            boolean phoneCompletionRequired = !userService.hasPhoneNumber(response.getUser());
            if (phoneCompletionRequired) {
                session.setAttribute("forcePhoneCompletion", true);
            } else {
                session.removeAttribute("forcePhoneCompletion");
            }
            
            // Return a simple response instead of AuthResponse with User
            RegisterResponseDTO dto = new RegisterResponseDTO(true, response.getMessage(), response.getUser().getEmail(), response.getUser().getId(), phoneCompletionRequired);
            System.out.println("Returning simple response: success=" + dto.isSuccess());
            return ResponseEntity.ok(dto);
        } else {
            System.out.println("Registration failed, returning bad request");
            return ResponseEntity.badRequest().body(new RegisterResponseDTO(false, response.getMessage(), null, null));
        }
    }
    


    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpSession session) {
        System.out.println("=== LOGOUT ===");
        System.out.println("Session ID before logout: " + session.getId());
        User user = (User) session.getAttribute("user");
        if (user != null) {
            System.out.println("Logging out user: " + user.getEmail());
        } else {
            System.out.println("No user found in session for logout");
        }
        
        session.invalidate();
        System.out.println("Session invalidated");
        return ResponseEntity.ok(new AuthResponse(true, "Deconectare reușită"));
    }

    @GetMapping("/user")
    public ResponseEntity<User> getCurrentUser(HttpSession session) {
        // Checking current user for session
        User sessionUser = (User) session.getAttribute("user");
        if (sessionUser != null) {
            System.out.println("User found in session: " + sessionUser.getEmail());
            
            // Citim utilizatorul din baza de date pentru a avea rolurile actualizate
            try {
                User freshUser = userService.getUserById(sessionUser.getId()).orElse(null);
                if (freshUser != null) {
                    System.out.println("Fresh user loaded from database with " + freshUser.getRoles().size() + " roles");
                    // Actualizăm sesiunea cu utilizatorul proaspăt
                    session.setAttribute("user", freshUser);
                    // Returnăm utilizatorul cu numărul de telefon formatat pentru afișare
                    User formattedUser = userService.getUserWithFormattedPhone(freshUser);
                    return ResponseEntity.ok(formattedUser);
                } else {
                    System.out.println("User not found in database");
                    return ResponseEntity.notFound().build();
                }
            } catch (Exception e) {
                System.out.println("Error loading fresh user: " + e.getMessage());
                // Returnăm utilizatorul din sesiune ca fallback cu numărul formatat
                User formattedUser = userService.getUserWithFormattedPhone(sessionUser);
                return ResponseEntity.ok(formattedUser);
            }
        } else {
            // No user found in session
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/check")
    public ResponseEntity<Map<String, Object>> checkAuth(HttpSession session) {
        // Auth check processing
        
        Map<String, Object> response = new HashMap<>();
        User sessionUser = (User) session.getAttribute("user");
        
        if (sessionUser != null) {
            // User found in session
            
            // Citim utilizatorul din baza de date pentru a avea rolurile actualizate
            try {
                User freshUser = userService.getUserById(sessionUser.getId()).orElse(null);
                if (freshUser != null) {
                    // Fresh user loaded from database
                    // Actualizăm sesiunea cu utilizatorul proaspăt
                    session.setAttribute("user", freshUser);
                    // Returnăm utilizatorul cu numărul de telefon formatat pentru afișare
                    User formattedUser = userService.getUserWithFormattedPhone(freshUser);
                    response.put("authenticated", true);
                    response.put("user", formattedUser);
                    response.put("sessionId", session.getId());
                    response.put("sessionTimeout", session.getMaxInactiveInterval());
                } else {
                    // User not found in database
                    response.put("authenticated", false);
                    response.put("sessionId", session.getId());
                }
            } catch (Exception e) {
                // Error loading fresh user
                // Folosim utilizatorul din sesiune ca fallback cu numărul formatat
                User formattedUser = userService.getUserWithFormattedPhone(sessionUser);
                response.put("authenticated", true);
                response.put("user", formattedUser);
                response.put("sessionId", session.getId());
                response.put("sessionTimeout", session.getMaxInactiveInterval());
            }
        } else {
            // No user found in session
            response.put("authenticated", false);
            response.put("sessionId", session.getId());
        }
        
        // Auth check response prepared
        return ResponseEntity.ok(response);
    }
    
    
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody ForgotPasswordRequest request, HttpServletRequest httpRequest) {

        // Monitor request for security threats
        securityMonitoringService.monitorRequest(httpRequest);
        
        // Verify reCAPTCHA
        if (!recaptchaService.verifyRecaptcha(request.getRecaptchaResponse(), getClientIpAddress(httpRequest))) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "Verificarea reCAPTCHA a eșuat. Vă rugăm să încercați din nou.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            boolean success = passwordResetService.initiatePasswordReset(request.getEmail());
            if (success) {
                response.put("success", true);
                response.put("message", "Link-ul de resetare a parolei a fost trimis pe email.");
                System.out.println("Password reset initiated successfully");
            } else {
                response.put("success", false);
                response.put("message", "Dacă adresa de email există în sistem, veți primi un link de resetare.");
                System.out.println("Password reset failed - user not found");
            }
        } catch (Exception e) {
            System.err.println("=== FORGOT PASSWORD ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            
            response.put("success", false);
            response.put("message", "Eroare: " + e.getMessage());
        }
        
        System.out.println("=== FORGOT PASSWORD RESPONSE ===");
        System.out.println("Response: " + response);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody ResetPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String userEmail = passwordResetService.resetPasswordAndGetEmail(request.getToken(), request.getNewPassword());
            if (userEmail != null) {
                response.put("success", true);
                response.put("message", "Parola a fost resetată cu succes!");
                response.put("email", userEmail);
            } else {
                response.put("success", false);
                response.put("message", "Token-ul este invalid sau a expirat.");
            }
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "A apărut o eroare. Încearcă din nou.");
        }
        
        return ResponseEntity.ok(response);
    }
}
