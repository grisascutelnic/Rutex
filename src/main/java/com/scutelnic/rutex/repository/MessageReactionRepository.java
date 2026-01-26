package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.MessageReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageReactionRepository extends JpaRepository<MessageReaction, Long> {

    Optional<MessageReaction> findByMessageIdAndUserId(Long messageId, Long userId);

    @Query("select r from MessageReaction r where r.message.id in :messageIds")
    List<MessageReaction> findByMessageIds(@Param("messageIds") List<Long> messageIds);
}
