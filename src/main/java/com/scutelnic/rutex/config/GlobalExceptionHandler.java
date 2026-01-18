package com.scutelnic.rutex.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleAllExceptions(Exception ex, WebRequest request) {
        System.out.println("=== GLOBAL EXCEPTION HANDLER ===");
        System.out.println("Exception type: " + ex.getClass().getName());
        System.out.println("Exception message: " + ex.getMessage());
        System.out.println("Request URI: " + request.getDescription(false));
        ex.printStackTrace();
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "A apărut o eroare internă: " + ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.internalServerError().body(response);
    }
    
    @ExceptionHandler(org.springframework.web.bind.MissingServletRequestParameterException.class)
    public ResponseEntity<Object> handleMissingParameter(org.springframework.web.bind.MissingServletRequestParameterException ex, WebRequest request) {
        System.out.println("=== MISSING PARAMETER EXCEPTION ===");
        System.out.println("Parameter name: " + ex.getParameterName());
        System.out.println("Parameter type: " + ex.getParameterType());
        System.out.println("Request URI: " + request.getDescription(false));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Parametrul lipsă: " + ex.getParameterName());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleHttpMessageNotReadable(org.springframework.http.converter.HttpMessageNotReadableException ex, WebRequest request) {
        System.out.println("=== HTTP MESSAGE NOT READABLE EXCEPTION ===");
        System.out.println("Exception message: " + ex.getMessage());
        System.out.println("Request URI: " + request.getDescription(false));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Eroare la citirea datelor: " + ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(org.springframework.web.multipart.MultipartException.class)
    public ResponseEntity<Object> handleMultipartException(org.springframework.web.multipart.MultipartException ex, WebRequest request) {
        System.out.println("=== MULTIPART EXCEPTION ===");
        System.out.println("Exception message: " + ex.getMessage());
        System.out.println("Request URI: " + request.getDescription(false));
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Eroare la încărcarea fișierului: " + ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Object> handleNoResourceFound(org.springframework.web.servlet.resource.NoResourceFoundException ex, WebRequest request) {
        // Ignore Chrome DevTools requests and other static resource requests
        String requestUri = request.getDescription(false);
        if (requestUri.contains("/.well-known/") || requestUri.contains("/favicon") || requestUri.contains("/robots.txt")) {
            return ResponseEntity.notFound().build();
        }
        
        System.out.println("=== NO RESOURCE FOUND EXCEPTION ===");
        System.out.println("Exception message: " + ex.getMessage());
        System.out.println("Request URI: " + requestUri);
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "Resursa nu a fost găsită: " + ex.getMessage());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(404).body(response);
    }
}
