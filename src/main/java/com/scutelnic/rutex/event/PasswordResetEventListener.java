package com.scutelnic.rutex.event;

import com.scutelnic.rutex.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetEventListener {

    @Autowired
    private EmailService emailService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void handlePasswordResetEmail(PasswordResetEmailEvent event) {
        emailService.sendPasswordResetEmail(event.getEmail(), event.getResetLink());
    }
}
