package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Rating;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.RatingRepository;
import com.scutelnic.rutex.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RatingService {
    
    @Autowired
    private RatingRepository ratingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Add or update a rating for a user
     */
    public Rating addOrUpdateRating(Long raterId, Long ratedUserId, Integer rating, String comment) {
        try {
            // Validate rating value
            if (rating < 1 || rating > 5) {
                throw new IllegalArgumentException("Rating must be between 1 and 5");
            }
            
            // Check if users exist
            User rater = userRepository.findById(raterId)
                    .orElseThrow(() -> new RuntimeException("Rater user not found with ID: " + raterId));
            
            User ratedUser = userRepository.findById(ratedUserId)
                    .orElseThrow(() -> new RuntimeException("Rated user not found with ID: " + ratedUserId));
            
            // Check if user is trying to rate themselves
            if (raterId.equals(ratedUserId)) {
                throw new RuntimeException("Users cannot rate themselves");
            }
            
            // Check if rating already exists
            List<Rating> existingRatings = ratingRepository.findByRaterIdAndRatedUserId(raterId, ratedUserId);
            
            Rating savedRating;
            if (!existingRatings.isEmpty()) {
                // Update existing rating (take the most recent one)
                Rating ratingToUpdate = existingRatings.get(0); // Most recent due to ORDER BY createdAt DESC
                ratingToUpdate.setRating(rating);
                ratingToUpdate.setComment(comment);
                savedRating = ratingRepository.save(ratingToUpdate);
                
                // If there are multiple ratings, delete the duplicates
                if (existingRatings.size() > 1) {
                    for (int i = 1; i < existingRatings.size(); i++) {
                        ratingRepository.delete(existingRatings.get(i));
                    }
                }
            } else {
                // Create new rating
                Rating newRating = new Rating();
                newRating.setRater(rater);
                newRating.setRatedUser(ratedUser);
                newRating.setRating(rating);
                newRating.setComment(comment);
                savedRating = ratingRepository.save(newRating);
            }
            
            // Update user's rating statistics
            updateUserRatingStats(ratedUserId);
            
            return savedRating;
            
        } catch (Exception e) {
            throw e;
        }
    }
    
    /**
     * Get all ratings for a specific user
     */
    public List<Rating> getUserRatings(Long userId) {
        return ratingRepository.findByRatedUserIdOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Get average rating for a user
     */
    public Double getAverageRating(Long userId) {
        Double avgRating = ratingRepository.getAverageRatingByUserId(userId);
        return avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0;
    }
    
    /**
     * Get total number of ratings for a user
     */
    public Long getTotalRatings(Long userId) {
        return ratingRepository.getTotalRatingsByUserId(userId);
    }
    
    /**
     * Check if a user has already rated another user
     */
    public boolean hasUserRated(Long raterId, Long ratedUserId) {
        return ratingRepository.existsByRaterIdAndRatedUserId(raterId, ratedUserId);
    }
    
    /**
     * Get existing rating if user has already rated
     */
    public Optional<Rating> getExistingRating(Long raterId, Long ratedUserId) {
        List<Rating> ratings = ratingRepository.findByRaterIdAndRatedUserId(raterId, ratedUserId);
        return ratings.isEmpty() ? Optional.empty() : Optional.of(ratings.get(0));
    }
    
    /**
     * Delete a rating
     */
    public void deleteRating(Long ratingId, Long raterId) {
        Optional<Rating> rating = ratingRepository.findById(ratingId);
        if (rating.isPresent() && rating.get().getRater().getId().equals(raterId)) {
            Long ratedUserId = rating.get().getRatedUser().getId();
            ratingRepository.deleteById(ratingId);
            
            // Update user's rating statistics
            updateUserRatingStats(ratedUserId);
        } else {
            throw new RuntimeException("Rating not found or user not authorized to delete it");
        }
    }
    
    /**
     * Update user's rating statistics
     */
    public void updateUserRatingStats(Long userId) {
        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
            
            Double avgRating = getAverageRating(userId);
            Long totalRatings = getTotalRatings(userId);
            
            user.setAverageRating(avgRating);
            user.setTotalRatings(totalRatings);
            
            userRepository.save(user);
            
        } catch (Exception e) {
            // Log error silently
        }
    }
}
