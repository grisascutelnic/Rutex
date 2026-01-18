package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.PasswordResetToken;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.PasswordResetTokenRepository;
import com.scutelnic.rutex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class PasswordResetService {
    
    @Autowired
    private PasswordResetTokenRepository tokenRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
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
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            System.out.println("Sending email to: " + email);
            emailService.sendPasswordResetEmail(email, resetLink);
            System.out.println("Email sent successfully");
            
            System.out.println("=== PASSWORD RESET SUCCESS ===");
            return true;
            
        } catch (Exception e) {
            System.err.println("=== PASSWORD RESET ERROR ===");
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            throw e; // Re-throw pentru a vedea eroarea în controller
        }
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
