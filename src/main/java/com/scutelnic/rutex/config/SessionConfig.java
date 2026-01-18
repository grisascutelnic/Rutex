package com.scutelnic.rutex.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;
import org.springframework.session.web.context.AbstractHttpSessionApplicationInitializer;

@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 15 * 24 * 60 * 60) // Default 15 days to allow remember me
public class SessionConfig extends AbstractHttpSessionApplicationInitializer {
    
    // This configuration allows Spring Session JDBC to work properly
    // The maxInactiveIntervalInSeconds is set to 15 days to allow remember me functionality
    // The actual timeout will be set by our custom logic in AuthController
    
    @Bean
    public org.springframework.session.web.http.CookieSerializer cookieSerializer() {
        org.springframework.session.web.http.DefaultCookieSerializer serializer = new org.springframework.session.web.http.DefaultCookieSerializer();
        serializer.setCookieName("SESSION");
        serializer.setCookiePath("/");
        serializer.setDomainNamePattern("^.+?\\.(\\w+\\.[a-z]+)$");
        serializer.setCookieMaxAge(15 * 24 * 60 * 60); // 15 days in seconds
        serializer.setUseHttpOnlyCookie(true);
        serializer.setUseSecureCookie(false); // Set to true in production with HTTPS
        return serializer;
    }
}
