package com.scutelnic.rutex.event;

public class NotificationPushEvent {

    private final Long userId;
    private final String titleRo;
    private final String messageRo;
    private final String titleRu;
    private final String messageRu;

    public NotificationPushEvent(Long userId, String titleRo, String messageRo, String titleRu, String messageRu) {
        this.userId = userId;
        this.titleRo = titleRo;
        this.messageRo = messageRo;
        this.titleRu = titleRu;
        this.messageRu = messageRu;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTitleRo() {
        return titleRo;
    }

    public String getMessageRo() {
        return messageRo;
    }

    public String getTitleRu() {
        return titleRu;
    }

    public String getMessageRu() {
        return messageRu;
    }
}
