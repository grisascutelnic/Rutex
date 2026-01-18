package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

@Entity
@Table(name = "ratings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Rating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rater_id", nullable = false)
    @JsonIgnore
    private User rater; // User who gives the rating
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rated_user_id", nullable = false)
    @JsonIgnore
    private User ratedUser; // User who receives the rating
    
    @Column(nullable = false)
    private Integer rating; // Rating value (1-5)
    
    @Column(length = 500)
    private String comment; // Optional comment
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    // JSON properties for frontend
    @JsonProperty("raterId")
    public Long getRaterId() {
        return rater != null ? rater.getId() : null;
    }
    
    @JsonProperty("ratedUserId")
    public Long getRatedUserId() {
        return ratedUser != null ? ratedUser.getId() : null;
    }
    
    @JsonProperty("raterName")
    public String getRaterName() {
        return rater != null ? rater.getFirstName() + " " + rater.getLastName() : null;
    }
    
    @JsonProperty("ratedUserName")
    public String getRatedUserName() {
        return ratedUser != null ? ratedUser.getFirstName() + " " + ratedUser.getLastName() : null;
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
