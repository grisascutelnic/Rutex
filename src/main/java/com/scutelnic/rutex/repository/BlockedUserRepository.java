package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    Optional<BlockedUser> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("select (count(b) > 0) from BlockedUser b " +
            "where (b.blocker.id = :userA and b.blocked.id = :userB) " +
            "or (b.blocker.id = :userB and b.blocked.id = :userA)")
    boolean existsBlockBetween(@Param("userA") Long userA, @Param("userB") Long userB);

    @Query("select b from BlockedUser b " +
            "where (b.blocker.id = :userA and b.blocked.id = :userB) " +
            "or (b.blocker.id = :userB and b.blocked.id = :userA)")
    Optional<BlockedUser> findBlockBetween(@Param("userA") Long userA, @Param("userB") Long userB);

    List<BlockedUser> findByBlockerId(Long blockerId);

    @Query("select b from BlockedUser b where b.blocker.id = :blockerId and b.blocked.id in :blockedIds")
    List<BlockedUser> findByBlockerIdAndBlockedIdIn(@Param("blockerId") Long blockerId, @Param("blockedIds") List<Long> blockedIds);

    @Query("select b from BlockedUser b where b.blocked.id = :blockedId and b.blocker.id in :blockerIds")
    List<BlockedUser> findByBlockedIdAndBlockerIdIn(@Param("blockedId") Long blockedId, @Param("blockerIds") List<Long> blockerIds);
}
