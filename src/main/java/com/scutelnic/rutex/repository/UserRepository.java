package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByEmail(String email);
    
    Optional<User> findByEmailAndIsActiveTrue(String email);
    
    boolean existsByEmail(String email);
    
    /**
     * Returnează ultimii N utilizatori creați, ordonați descrescător după data creării
     * @param limit Numărul de utilizatori de returnat
     * @return Lista cu ultimii N utilizatori
     */
    List<User> findTop70ByOrderByCreatedAtDesc();
}
