package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.PushSubscription;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    Optional<PushSubscription> findByEndpoint(String endpoint);

    List<PushSubscription> findByUserId(Long userId);

    @Modifying
    @Transactional
    int deleteByUserIdAndEndpoint(Long userId, String endpoint);

    @Modifying
    @Transactional
    int deleteByEndpoint(String endpoint);
}
