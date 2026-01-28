package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.ContactActionEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ContactActionEventRepository extends JpaRepository<ContactActionEvent, Long> {

    long countByActionType(String actionType);

    @Query("SELECT COUNT(e) FROM ContactActionEvent e WHERE e.actionType = :actionType AND e.createdAt >= :since")
    long countByActionTypeSince(@Param("actionType") String actionType, @Param("since") LocalDateTime since);
}
