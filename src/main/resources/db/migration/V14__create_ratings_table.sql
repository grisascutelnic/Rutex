-- Create ratings table
CREATE TABLE IF NOT EXISTS ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rater_id BIGINT NOT NULL,
    rated_user_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    -- Foreign key constraints
    FOREIGN KEY (rater_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (rated_user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- Unique constraint to prevent duplicate ratings
    UNIQUE KEY unique_user_rating (rater_id, rated_user_id),
    
    -- Indexes for performance
    INDEX idx_rater_id (rater_id),
    INDEX idx_rated_user_id (rated_user_id),
    INDEX idx_rating (rating),
    INDEX idx_created_at (created_at)
);

-- Add rating statistics columns to users table if they don't exist
ALTER TABLE users 
ADD COLUMN IF NOT EXISTS average_rating DECIMAL(3,2) DEFAULT 0.00,
ADD COLUMN IF NOT EXISTS total_ratings INT DEFAULT 0;
