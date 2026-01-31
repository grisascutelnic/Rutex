package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.ConversationDeletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationDeletionRepository extends JpaRepository<ConversationDeletion, Long> {

    Optional<ConversationDeletion> findByConversationIdAndUserId(Long conversationId, Long userId);

    @Query("select d from ConversationDeletion d where d.user.id = :userId and d.conversation.id in :conversationIds")
    List<ConversationDeletion> findByUserIdAndConversationIdIn(@Param("userId") Long userId,
                                                              @Param("conversationIds") List<Long> conversationIds);
}
