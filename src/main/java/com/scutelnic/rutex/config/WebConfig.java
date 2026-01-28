package com.scutelnic.rutex.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*
I created this class, because i was getting error 405 Method Not Allowed when i try to delete a tour
and also i was getting error with Request method 'POST' is not supported.

This class is i think for @DeleteMapping and @PutMapping
 */

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private VisitorTrackingInterceptor visitorTrackingInterceptor;
    
    @Autowired
    private BanInterceptor banInterceptor;

    @Autowired
    private UserActivityInterceptor userActivityInterceptor;

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
        
        // Configure favicon
        registry.addResourceHandler("/favicon.ico")
                .addResourceLocations("classpath:/static/");
        registry.addResourceHandler("/favicon.svg")
                .addResourceLocations("classpath:/static/");
    }
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .maxAge(3600)
                .allowCredentials(false);
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Ban interceptor - runs first to block banned IPs
        registry.addInterceptor(banInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "*.ico", "*.css", "*.js", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg");
        
        // Visitor tracking interceptor - runs after ban check
        registry.addInterceptor(visitorTrackingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/admin/**", "/api/**", "/css/**", "/js/**", "/images/**", "/uploads/**", "*.ico", "*.css", "*.js", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg");

        // User activity tracking for online/last seen
        registry.addInterceptor(userActivityInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/css/**", "/js/**", "/images/**", "/uploads/**", "*.ico", "*.css", "*.js", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.svg");
    }
}
