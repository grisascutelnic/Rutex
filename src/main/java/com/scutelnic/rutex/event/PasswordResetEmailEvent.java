package com.scutelnic.rutex.event;

public class PasswordResetEmailEvent {

    private final String email;
    private final String resetLink;

    public PasswordResetEmailEvent(String email, String resetLink) {
        this.email = email;
        this.resetLink = resetLink;
    }

    public String getEmail() {
        return email;
    }

    public String getResetLink() {
        return resetLink;
    }
}
