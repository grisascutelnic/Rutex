package com.scutelnic.rutex.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import lombok.Data;

@Configuration
@ConfigurationProperties(prefix = "recaptcha")
@Data
public class RecaptchaConfig {
    private String siteKey;
    private String secretKey;
    private boolean enabled = false;
}
