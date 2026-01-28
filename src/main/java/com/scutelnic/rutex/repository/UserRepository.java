package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);

    List<User> findByIsActiveTrue();
    
    boolean existsByEmail(String email);
    
    /**
     * Returnează ultimii N utilizatori creați, ordonați descrescător după data creării
     * @param limit Numărul de utilizatori de returnat
     * @return Lista cu ultimii N utilizatori
     */
    List<User> findTop70ByOrderByCreatedAtDesc();

    @Modifying
    @Transactional
    @Query("update User u set u.lastSeenAt = :lastSeenAt where u.id = :userId")
    int updateLastSeenAt(@Param("userId") Long userId, @Param("lastSeenAt") LocalDateTime lastSeenAt);

    @Query("select u from User u where u.isActive = true and u.lastSeenAt >= :since order by u.lastSeenAt desc")
    List<User> findActiveUsersSince(@Param("since") LocalDateTime since);
}
