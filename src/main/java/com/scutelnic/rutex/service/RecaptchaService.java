package com.scutelnic.rutex.service;

import com.scutelnic.rutex.config.RecaptchaConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import java.util.Map;

@Service
public class RecaptchaService {
    
    @Autowired
    private RecaptchaConfig recaptchaConfig;
    
    @Autowired
    private RestTemplate restTemplate;
    
    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";
    
    public boolean verifyRecaptcha(String recaptchaResponse, String clientIp) {
        if (!recaptchaConfig.isEnabled()) {
            return true; // Skip verification if disabled
        }
        
        if (recaptchaResponse == null || recaptchaResponse.isEmpty()) {
            return false;
        }
        
        try {
            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("secret", recaptchaConfig.getSecretKey());
            params.add("response", recaptchaResponse);
            params.add("remoteip", clientIp);
            
            Map<String, Object> response = restTemplate.postForObject(
                RECAPTCHA_VERIFY_URL, 
                params, 
                Map.class
            );
            
            if (response != null && response.containsKey("success")) {
                return Boolean.TRUE.equals(response.get("success"));
            }
            
            return false;
        } catch (Exception e) {
            System.err.println("Error verifying reCAPTCHA: " + e.getMessage());
            return false;
        }
    }
    
    public String getSiteKey() {
        return recaptchaConfig.getSiteKey();
    }
    
    public boolean isEnabled() {
        return recaptchaConfig.isEnabled();
    }
}
