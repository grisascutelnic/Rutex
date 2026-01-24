package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.PushSubscriptionRequest;
import com.scutelnic.rutex.entity.PushSubscription;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.PushSubscriptionRepository;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PushSubscriptionService {

    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;

    public PushSubscription saveSubscription(User user, PushSubscriptionRequest request) {
        Optional<PushSubscription> existing = pushSubscriptionRepository.findByEndpoint(request.getEndpoint());
        PushSubscription subscription = existing.orElseGet(PushSubscription::new);
        subscription.setUser(user);
        subscription.setEndpoint(request.getEndpoint());
        subscription.setP256dh(request.getKeys().getP256dh());
        subscription.setAuth(request.getKeys().getAuth());
        subscription.setLanguage(normalizeLanguage(request.getLanguage()));
        subscription.setUserAgent(request.getUserAgent());
        return pushSubscriptionRepository.save(subscription);
    }

    @Transactional
    public boolean removeSubscription(User user, String endpoint) {
        if (endpoint == null || endpoint.isBlank()) {
            return false;
        }
        return pushSubscriptionRepository.deleteByUserIdAndEndpoint(user.getId(), endpoint) > 0;
    }

    private String normalizeLanguage(String language) {
        if ("ru".equalsIgnoreCase(language)) {
            return "ru";
        }
        return "ro";
    }
}
