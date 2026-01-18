package com.scutelnic.rutex.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.TimeZone;
import jakarta.annotation.PostConstruct;

@Configuration
public class TimezoneConfig {
    
    @PostConstruct
    public void setDefaultTimezone() {
        // Setăm timezone-ul implicit pentru întreaga aplicație
        TimeZone.setDefault(TimeZone.getTimeZone("Europe/Bucharest"));
        
        // Forțăm și timezone-ul pentru sistem
        System.setProperty("user.timezone", "Europe/Bucharest");
    }
    
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setTimeZone(TimeZone.getTimeZone("Europe/Bucharest"));
        
        return mapper;
    }
}
