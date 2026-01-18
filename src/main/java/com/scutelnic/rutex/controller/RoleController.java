package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.UserService;
import com.scutelnic.rutex.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
public class RoleController {

    @Autowired
    private UserService userService;

    @PostMapping("/users/makeAdmin")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> makeAdmin(@RequestParam(name = "id") Long id, HttpSession session) {
        // Verificăm dacă utilizatorul este logat și are rol de admin
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fiți logat pentru a efectua această acțiune.");
            return ResponseEntity.status(401).body(response);
        }
        
        boolean isAdmin = currentUser.getRoles() != null && 
                         currentUser.getRoles().stream()
                         .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        
        if (!isAdmin) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Nu aveți permisiunea de a efectua această acțiune.");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            userService.updateUserRole(id, "ROLE_ADMIN");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilizatorul a fost promovat la rolul de ADMIN");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/users/makeMod")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> makeMod(@RequestParam(name = "id") Long id, HttpSession session) {
        // Verificăm dacă utilizatorul este logat și are rol de admin
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fiți logat pentru a efectua această acțiune.");
            return ResponseEntity.status(401).body(response);
        }
        
        boolean isAdmin = currentUser.getRoles() != null && 
                         currentUser.getRoles().stream()
                         .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        
        if (!isAdmin) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Nu aveți permisiunea de a efectua această acțiune.");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            userService.updateUserRole(id, "ROLE_MOD");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilizatorul a fost promovat la rolul de MODERATOR");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    @PostMapping("/users/makeUser")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> makeUser(@RequestParam(name = "id") Long id, HttpSession session) {
        // Verificăm dacă utilizatorul este logat și are rol de admin
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Trebuie să fiți logat pentru a efectua această acțiune.");
            return ResponseEntity.status(401).body(response);
        }
        
        boolean isAdmin = currentUser.getRoles() != null && 
                         currentUser.getRoles().stream()
                         .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        
        if (!isAdmin) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Nu aveți permisiunea de a efectua această acțiune.");
            return ResponseEntity.status(403).body(response);
        }
        
        try {
            userService.updateUserRole(id, "ROLE_USER");
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilizatorul a fost setat la rolul de USER");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
