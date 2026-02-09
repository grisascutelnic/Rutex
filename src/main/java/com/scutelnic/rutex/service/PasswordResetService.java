package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.PasswordResetToken;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.event.PasswordResetEmailEvent;
import com.scutelnic.rutex.repository.PasswordResetTokenRepository;
import com.scutelnic.rutex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String appBaseUrl;
    
    @Transactional
    public boolean initiatePasswordReset(String email) {
        System.out.println("=== PASSWORD RESET INITIATED ===");
        System.out.println("Email: " + email);
        
        try {
            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                System.out.println("User not found for email: " + email);
                return false;
            }
            
            User user = userOpt.get();
            System.out.println("User found: " + user.getEmail() + " (ID: " + user.getId() + ")");
            
            // Delete any existing tokens for this user using a more robust approach
            System.out.println("Deleting existing tokens for user ID: " + user.getId());
            try {
                List<PasswordResetToken> existingTokens = tokenRepository.findByUser_Id(user.getId());
                System.out.println("Found " + existingTokens.size() + " existing tokens");
                
                if (!existingTokens.isEmpty()) {
                    tokenRepository.deleteAll(existingTokens);
                    System.out.println("Successfully deleted " + existingTokens.size() + " existing tokens");
                }
            } catch (Exception e) {
                System.err.println("Error deleting existing tokens: " + e.getMessage());
                // Continue with creating new token even if deletion fails
            }
            
            // Create new token
            String token = UUID.randomUUID().toString();
            System.out.println("Generated new token: " + token);
            
            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(LocalDateTime.now().plusHours(1));
            resetToken.setUsed(false);
            
            tokenRepository.save(resetToken);
            System.out.println("Token saved successfully");
            
            // Send email
            String resetLink = buildResetLink(token);
            System.out.println("Sending email to: " + email);
            eventPublisher.publishEvent(new PasswordResetEmailEvent(email, resetLink));
            System.out.println("Password reset email queued");
            
            System.out.println("=== PASSWORD RESET SUCCESS ===");
            return true;
            
        } catch (Exception e) {
            System.err.println("=== PASSWORD RESET ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw pentru a vedea eroarea în controller
        }
    }

    private String buildResetLink(String token) {
        String baseUrl = appBaseUrl != null ? appBaseUrl.trim() : "http://localhost:8080";
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        return baseUrl + "/reset-password?token=" + token;
    }
    
    public boolean validateToken(String token) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        return !resetToken.isExpired();
    }
    
    @Transactional
    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token);
        if (tokenOpt.isEmpty()) {
            return false;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isExpired()) {
            return false;
        }
        
        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        return true;
    }
    
    @Transactional
    public String resetPasswordAndGetEmail(String token, String newPassword) {
        Optional<PasswordResetToken> tokenOpt = tokenRepository.findByTokenAndUsedFalse(token);
        if (tokenOpt.isEmpty()) {
            return null;
        }
        
        PasswordResetToken resetToken = tokenOpt.get();
        if (resetToken.isExpired()) {
            return null;
        }
        
        // Update user password
        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        // Mark token as used
        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
        
        return user.getEmail();
    }
}
