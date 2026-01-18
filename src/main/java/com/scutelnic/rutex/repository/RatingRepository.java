package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    
    // Find all ratings for a specific user
    @Query("SELECT r FROM Rating r WHERE r.ratedUser.id = :ratedUserId ORDER BY r.createdAt DESC")
    List<Rating> findByRatedUserIdOrderByCreatedAtDesc(@Param("ratedUserId") Long ratedUserId);
    
    // Find rating given by a specific user to another specific user
    @Query("SELECT r FROM Rating r WHERE r.rater.id = :raterId AND r.ratedUser.id = :ratedUserId ORDER BY r.createdAt DESC")
    List<Rating> findByRaterIdAndRatedUserId(@Param("raterId") Long raterId, @Param("ratedUserId") Long ratedUserId);
    
    // Check if a user has already rated another user
    @Query("SELECT COUNT(r) > 0 FROM Rating r WHERE r.rater.id = :raterId AND r.ratedUser.id = :ratedUserId")
    boolean existsByRaterIdAndRatedUserId(@Param("raterId") Long raterId, @Param("ratedUserId") Long ratedUserId);
    
    // Get average rating for a user
    @Query("SELECT AVG(r.rating) FROM Rating r WHERE r.ratedUser.id = :userId")
    Double getAverageRatingByUserId(@Param("userId") Long userId);
    
    // Get total number of ratings for a user
    @Query("SELECT COUNT(r) FROM Rating r WHERE r.ratedUser.id = :userId")
    Long getTotalRatingsByUserId(@Param("userId") Long userId);
}
