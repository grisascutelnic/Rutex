package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.ContactActionEvent;
import com.scutelnic.rutex.repository.ContactActionEventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ContactActionService {

    public static final String ACTION_SHOW_CONTACTS = "show_contacts";
    public static final String ACTION_PHONE = "phone";
    public static final String ACTION_EMAIL = "email";

    private final ContactActionEventRepository contactActionEventRepository;

    public ContactActionService(ContactActionEventRepository contactActionEventRepository) {
        this.contactActionEventRepository = contactActionEventRepository;
    }

    public void recordAction(Long rideId, String actionType) {
        ContactActionEvent event = new ContactActionEvent(rideId, actionType, LocalDateTime.now());
        contactActionEventRepository.save(event);
    }

    public Map<String, Object> getContactActionStatistics() {
        Map<String, Object> stats = new HashMap<>();

        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime weekStart = LocalDateTime.now().minusDays(7);
        LocalDateTime monthStart = LocalDateTime.now().minusDays(30);

        addActionStats(stats, ACTION_SHOW_CONTACTS, "showContacts", todayStart, weekStart, monthStart);
        addActionStats(stats, ACTION_PHONE, "phone", todayStart, weekStart, monthStart);
        addActionStats(stats, ACTION_EMAIL, "email", todayStart, weekStart, monthStart);

        return stats;
    }

    private void addActionStats(Map<String, Object> stats, String actionType, String prefix,
                                LocalDateTime todayStart, LocalDateTime weekStart, LocalDateTime monthStart) {
        stats.put(prefix + "Total", contactActionEventRepository.countByActionType(actionType));
        stats.put(prefix + "Today", contactActionEventRepository.countByActionTypeSince(actionType, todayStart));
        stats.put(prefix + "Week", contactActionEventRepository.countByActionTypeSince(actionType, weekStart));
        stats.put(prefix + "Month", contactActionEventRepository.countByActionTypeSince(actionType, monthStart));
    }
}
