package com.scutelnic.rutex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scutelnic.rutex.entity.PushSubscription;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.PushSubscriptionRepository;
import java.security.Security;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import nl.martijndwars.webpush.Notification;
import nl.martijndwars.webpush.PushService;
import nl.martijndwars.webpush.Subscription;
import nl.martijndwars.webpush.Utils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.apache.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class PushNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(PushNotificationService.class);

    private final PushSubscriptionRepository pushSubscriptionRepository;
    private final ObjectMapper objectMapper;
    private final boolean enabled;
    private PushService pushService;

    @Autowired
    public PushNotificationService(
        PushSubscriptionRepository pushSubscriptionRepository,
        ObjectMapper objectMapper,
        @Value("${app.push.vapid.public-key:}") String publicKey,
        @Value("${app.push.vapid.private-key:}") String privateKey,
        @Value("${app.push.vapid.subject:mailto:contact@rutex.md}") String subject
    ) {
        this.pushSubscriptionRepository = pushSubscriptionRepository;
        this.objectMapper = objectMapper;
        boolean enabledValue = false;
        PushService localPushService = null;

        if (publicKey == null || publicKey.isBlank() || privateKey == null || privateKey.isBlank()) {
            logger.info("Push notifications disabled because VAPID keys are missing.");
        } else {
            try {
                if (Security.getProvider(BouncyCastleProvider.PROVIDER_NAME) == null) {
                    Security.addProvider(new BouncyCastleProvider());
                }
                localPushService = new PushService();
                localPushService.setPublicKey(Utils.loadPublicKey(publicKey));
                localPushService.setPrivateKey(Utils.loadPrivateKey(privateKey));
                localPushService.setSubject(subject);
                enabledValue = true;
            } catch (Exception ex) {
                logger.warn("Push notifications disabled due to VAPID configuration error.", ex);
                enabledValue = false;
            }
        }

        this.pushService = localPushService;
        this.enabled = enabledValue;
    }

    public void sendToUser(User user, String titleRo, String messageRo, String titleRu, String messageRu) {
        if (!enabled || user == null || user.getId() == null) {
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserId(user.getId());
        sendToSubscriptions(subscriptions, titleRo, messageRo, titleRu, messageRu);
    }

    public void sendToUserId(Long userId, String titleRo, String messageRo, String titleRu, String messageRu) {
        if (!enabled || userId == null) {
            return;
        }

        List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserId(userId);
        sendToSubscriptions(subscriptions, titleRo, messageRo, titleRu, messageRu);
    }

    private void sendToSubscriptions(List<PushSubscription> subscriptions, String titleRo, String messageRo, String titleRu, String messageRu) {
        if (subscriptions == null || subscriptions.isEmpty()) {
            return;
        }

        for (PushSubscription subscription : subscriptions) {
            String language = "ru".equalsIgnoreCase(subscription.getLanguage()) ? "ru" : "ro";
            String title = "ru".equals(language) ? titleRu : titleRo;
            String message = "ru".equals(language) ? messageRu : messageRo;
            String url = "/" + language;
            sendToSubscription(subscription, title, message, url);
        }
    }

    private void sendToSubscription(PushSubscription subscription, String title, String message, String url) {
        if (!enabled) {
            return;
        }

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("title", title);
            payload.put("message", message);
            payload.put("url", url);

            String jsonPayload = objectMapper.writeValueAsString(payload);
            Subscription.Keys keys = new Subscription.Keys(subscription.getP256dh(), subscription.getAuth());
            Subscription sub = new Subscription(subscription.getEndpoint(), keys);
            Notification notification = new Notification(sub, jsonPayload);
            HttpResponse response = pushService.send(notification);

            if (response != null && response.getStatusLine() != null) {
                int status = response.getStatusLine().getStatusCode();
                if (status == 404 || status == 410) {
                    pushSubscriptionRepository.deleteByEndpoint(subscription.getEndpoint());
                }
            }
        } catch (Exception ex) {
            logger.warn("Failed to send push notification for subscription {}", subscription.getId(), ex);
        }
    }
}
