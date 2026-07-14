package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    @Query("select c from Conversation c where c.userOne.id = :userId or c.userTwo.id = :userId order by c.updatedAt desc")
    List<Conversation> findAllForUser(@Param("userId") Long userId);

    @Query("select distinct c from Conversation c join Message m on m.conversation = c " +
           "where c.userOne.id = :userId or c.userTwo.id = :userId order by c.updatedAt desc")
    List<Conversation> findAllForUserWithMessages(@Param("userId") Long userId);

    @Query("select c from Conversation c left join Message m on m.conversation = c " +
           "where (c.userOne.id = :userId or c.userTwo.id = :userId) and m.id is null")
    List<Conversation> findEmptyForUser(@Param("userId") Long userId);

    @Query("select c from Conversation c where c.lastMessage is not null order by c.updatedAt desc")
    List<Conversation> findAllWithMessages();
}
