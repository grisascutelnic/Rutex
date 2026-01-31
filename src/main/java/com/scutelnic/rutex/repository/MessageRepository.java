package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    long countByConversationId(Long conversationId);

    List<Message> findByConversationIdOrderByIdDesc(Long conversationId, Pageable pageable);

    List<Message> findByConversationIdAndIdLessThanOrderByIdDesc(Long conversationId, Long beforeId, Pageable pageable);

    List<Message> findByConversationIdAndCreatedAtAfterOrderByIdDesc(Long conversationId, LocalDateTime after, Pageable pageable);

    List<Message> findByConversationIdAndCreatedAtAfterAndIdLessThanOrderByIdDesc(Long conversationId, LocalDateTime after, Long beforeId, Pageable pageable);

    Message findTopByConversationIdAndCreatedAtAfterOrderByIdDesc(Long conversationId, LocalDateTime after);

    @Query("select count(m) from Message m where (m.conversation.userOne.id = :userId or m.conversation.userTwo.id = :userId) and m.sender.id <> :userId and m.readAt is null")
    long countUnreadForUser(@Param("userId") Long userId);

    @Query("select m.conversation.id, count(m) from Message m where m.conversation.id in :conversationIds and m.sender.id <> :userId and m.readAt is null group by m.conversation.id")
    List<Object[]> countUnreadByConversation(@Param("conversationIds") List<Long> conversationIds, @Param("userId") Long userId);

    @Query("select count(m) from Message m where m.conversation.id = :conversationId and m.sender.id <> :userId and m.readAt is null and m.createdAt > :after")
    long countUnreadForConversationAfter(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("after") LocalDateTime after);

    @Query("select count(m) from Message m where m.conversation.id = :conversationId and m.sender.id <> :userId and m.readAt is null")
    long countUnreadForConversation(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Modifying
    @Query("update Message m set m.readAt = :readAt where m.conversation.id = :conversationId and m.sender.id <> :userId and m.readAt is null and m.id <= :lastMessageId")
    int markReadUpTo(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("lastMessageId") Long lastMessageId, @Param("readAt") LocalDateTime readAt);

    @Modifying
    @Query("update Message m set m.readAt = :readAt where m.conversation.id = :conversationId and m.sender.id <> :userId and m.readAt is null and m.id <= :lastMessageId and m.createdAt > :after")
    int markReadUpToAfter(@Param("conversationId") Long conversationId, @Param("userId") Long userId, @Param("lastMessageId") Long lastMessageId, @Param("readAt") LocalDateTime readAt, @Param("after") LocalDateTime after);

    @Query("select m from Message m where (m.conversation.userOne.id = :userId or m.conversation.userTwo.id = :userId) and m.sender.id <> :userId and m.deliveredAt is null")
    List<Message> findUndeliveredForUser(@Param("userId") Long userId);
}
