package com.scutelnic.rutex.event;

import com.scutelnic.rutex.service.PushNotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class NotificationEventListener {

    @Autowired
    private PushNotificationService pushNotificationService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handleNotificationPush(NotificationPushEvent event) {
        pushNotificationService.sendToUserId(
            event.getUserId(),
            event.getTitleRo(),
            event.getMessageRo(),
            event.getTitleRu(),
            event.getMessageRu()
        );
    }
}
