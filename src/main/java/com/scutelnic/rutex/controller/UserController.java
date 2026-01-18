package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.UserService;
import com.scutelnic.rutex.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(user -> {
                    // Returnăm utilizatorul cu numărul de telefon formatat pentru afișare
                    User formattedUser = userService.getUserWithFormattedPhone(user);
                    return ResponseEntity.ok(formattedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @PostMapping
    public ResponseEntity<Map<String, Object>> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.createUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilizatorul a fost creat cu succes!");
            response.put("user", savedUser);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            user.setId(id);
            User updatedUser = userService.updateUser(user);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilizatorul a fost actualizat cu succes!");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la actualizarea utilizatorului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/update-profile")
    public ResponseEntity<Map<String, Object>> updateProfile(
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("email") String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "phonePrefix", required = false) String phonePrefix,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "profileImageUrl", required = false) String profileImageUrl,
            HttpSession session) {
        
        try {
            // Verificăm dacă utilizatorul este logat
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a actualiza profilul.");
                return ResponseEntity.status(401).body(response);
            }
            
            User updatedUser = userService.updateProfile(
                currentUser.getId(),
                firstName,
                lastName,
                email,
                phone,
                phonePrefix,
                currentPassword,
                newPassword,
                profileImageUrl
            );
            
            // Actualizăm sesiunea cu datele noi
            session.setAttribute("user", updatedUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Profilul a fost actualizat cu succes!");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Eroare la actualizarea profilului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Utilizatorul a fost șters cu succes!");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
