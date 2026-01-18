package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.Rating;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.RatingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.scutelnic.rutex.repository.RatingRepository;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {
    
    @Autowired
    private RatingService ratingService;
    
    @Autowired
    private RatingRepository ratingRepository;
    
    /**
     * Add or update a rating
     */
    @PostMapping("/rate")
    public ResponseEntity<Map<String, Object>> rateUser(
            @RequestParam(required = false) String ratedUserId,
            @RequestParam(required = false) String rating,
            @RequestParam(required = false) String comment,
            @RequestBody(required = false) Map<String, Object> requestBody,
            HttpSession session) {
        
        // Handle both FormData and JSON requests
        if (ratedUserId == null && requestBody != null) {
            ratedUserId = (String) requestBody.get("ratedUserId");
            rating = (String) requestBody.get("rating");
            comment = (String) requestBody.get("comment");
        }
        
        // Validate required parameters
        if (ratedUserId == null || rating == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Parametri obligatorii lipsesc.");
            return ResponseEntity.badRequest().body(response);
        }
        
        // Convert string parameters to proper types
        Long ratedUserIdLong;
        Integer ratingInt;
        
        try {
            ratedUserIdLong = Long.parseLong(ratedUserId);
            ratingInt = Integer.parseInt(rating);
        } catch (NumberFormatException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Parametri invalizi pentru rating.");
            return ResponseEntity.badRequest().body(response);
        }
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if user is logged in
            User currentUser = (User) session.getAttribute("user");
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a pune un rating.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Add or update rating
            Rating savedRating = ratingService.addOrUpdateRating(
                currentUser.getId(), 
                ratedUserIdLong, 
                ratingInt, 
                comment
            );
            
            response.put("success", true);
            response.put("message", "Rating-ul a fost salvat cu succes!");
            response.put("rating", savedRating);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la salvarea rating-ului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Update an existing rating
     */
    @PutMapping("/{ratingId}")
    public ResponseEntity<Map<String, Object>> updateRating(
            @PathVariable Long ratingId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            HttpSession session) {
        
        System.out.println("🔍 RatingController.updateRating called with: ratingId=" + ratingId + ", rating=" + rating);
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if user is logged in
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a actualiza un rating.");
                return ResponseEntity.status(401).body(response);
            }
            
            // Get the existing rating
            Optional<Rating> existingRating = ratingRepository.findById(ratingId);
            if (!existingRating.isPresent()) {
                response.put("success", false);
                response.put("message", "Rating-ul nu a fost găsit.");
                return ResponseEntity.notFound().build();
            }
            
            Rating ratingToUpdate = existingRating.get();
            
            // Check if user owns this rating
            if (!ratingToUpdate.getRater().getId().equals(currentUser.getId())) {
                response.put("success", false);
                response.put("message", "Nu aveți permisiunea să actualizați acest rating.");
                return ResponseEntity.status(403).body(response);
            }
            
            // Update rating
            ratingToUpdate.setRating(rating);
            ratingToUpdate.setComment(comment);
            
            Rating updatedRating = ratingRepository.save(ratingToUpdate);
            
            // Update user's rating statistics
            ratingService.updateUserRatingStats(ratingToUpdate.getRatedUser().getId());
            
            response.put("success", true);
            response.put("message", "Rating-ul a fost actualizat cu succes!");
            response.put("rating", updatedRating);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            System.out.println("❌ Error updating rating: " + e.getMessage());
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "Eroare la actualizarea rating-ului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Get all ratings for a user
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserRatings(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<Rating> ratings = ratingService.getUserRatings(userId);
            Double averageRating = ratingService.getAverageRating(userId);
            Long totalRatings = ratingService.getTotalRatings(userId);
            
            response.put("success", true);
            response.put("ratings", ratings);
            response.put("averageRating", averageRating);
            response.put("totalRatings", totalRatings);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la încărcarea rating-urilor: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Check if current user has rated a specific user
     */
    @GetMapping("/check/{ratedUserId}")
    public ResponseEntity<Map<String, Object>> checkUserRating(
            @PathVariable Long ratedUserId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = (User) session.getAttribute("user");
            
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Utilizatorul nu este autentificat.");
                response.put("hasRated", false);
                return ResponseEntity.ok(response);
            }
            
            boolean hasRated = ratingService.hasUserRated(currentUser.getId(), ratedUserId);
            
            response.put("success", true);
            response.put("hasRated", hasRated);
            
            if (hasRated) {
                // Get existing rating details
                var existingRating = ratingService.getExistingRating(currentUser.getId(), ratedUserId);
                if (existingRating.isPresent()) {
                    response.put("existingRating", existingRating.get());
                }
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la verificarea rating-ului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * Delete a rating
     */
    @DeleteMapping("/{ratingId}")
    public ResponseEntity<Map<String, Object>> deleteRating(
            @PathVariable Long ratingId,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            User currentUser = (User) session.getAttribute("user");
            if (currentUser == null) {
                response.put("success", false);
                response.put("message", "Trebuie să fiți logat pentru a șterge un rating.");
                return ResponseEntity.status(401).body(response);
            }
            
            ratingService.deleteRating(ratingId, currentUser.getId());
            
            response.put("success", true);
            response.put("message", "Rating-ul a fost șters cu succes!");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Eroare la ștergerea rating-ului: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
