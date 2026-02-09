package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Notification;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.event.NotificationPushEvent;
import com.scutelnic.rutex.repository.NotificationRepository;
import com.scutelnic.rutex.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private static final String WELCOME_TITLE_RO = "Bun venit pe Rutex!";
    private static final String WELCOME_MESSAGE_RO = "Contul tau a fost creat cu succes. Iti dorim calatorii placute!";
    private static final String WELCOME_TITLE_RU = "Добро пожаловать в Rutex!";
    private static final String WELCOME_MESSAGE_RU = "Ваш аккаунт успешно создан. Желаем приятных поездок!";

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public Notification createNotification(User user, String titleRo, String messageRo, String titleRu, String messageRu) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setTitleRo(titleRo);
        notification.setMessageRo(messageRo);
        notification.setTitleRu(titleRu);
        notification.setMessageRu(messageRu);
        Notification saved = notificationRepository.save(notification);
        if (user != null && user.getId() != null) {
            eventPublisher.publishEvent(new NotificationPushEvent(user.getId(), titleRo, messageRo, titleRu, messageRu));
        }
        return saved;
    }

    public void createWelcomeNotification(User user) {
        createNotification(user, WELCOME_TITLE_RO, WELCOME_MESSAGE_RO, WELCOME_TITLE_RU, WELCOME_MESSAGE_RU);
    }

    public List<Notification> getNotifications(Long userId, int limit) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit));
    }

    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndReadAtIsNull(userId);
    }

    public boolean markRead(Long userId, Long notificationId) {
        return notificationRepository.findByIdAndUserId(notificationId, userId)
            .map(notification -> {
                if (notification.getReadAt() == null) {
                    notification.setReadAt(LocalDateTime.now());
                    notificationRepository.save(notification);
                }
                return true;
            })
            .orElse(false);
    }

    public int markAllRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, 1000));
        int updated = 0;
        for (Notification notification : notifications) {
            if (notification.getReadAt() == null) {
                notification.setReadAt(LocalDateTime.now());
                updated++;
            }
        }
        if (updated > 0) {
            notificationRepository.saveAll(notifications);
        }
        return updated;
    }

    public int broadcastToAll(String titleRo, String messageRo, String titleRu, String messageRu) {
        List<User> users = userRepository.findByIsActiveTrue();
        List<Notification> notifications = new ArrayList<>();
        for (User user : users) {
            Notification notification = new Notification();
            notification.setUser(user);
            notification.setTitleRo(titleRo);
            notification.setMessageRo(messageRo);
            notification.setTitleRu(titleRu);
            notification.setMessageRu(messageRu);
            notifications.add(notification);
            if (user.getId() != null) {
                eventPublisher.publishEvent(new NotificationPushEvent(user.getId(), titleRo, messageRo, titleRu, messageRu));
            }
        }
        notificationRepository.saveAll(notifications);
        return notifications.size();
    }

    public boolean sendToUserByEmail(String email, String titleRo, String messageRo, String titleRu, String messageRu) {
        if (email == null || email.isBlank()) {
            return false;
        }
        return userRepository.findByEmailAndIsActiveTrue(email.trim())
            .map(user -> {
                createNotification(user, titleRo, messageRo, titleRu, messageRu);
                return true;
            })
            .orElse(false);
    }
}
