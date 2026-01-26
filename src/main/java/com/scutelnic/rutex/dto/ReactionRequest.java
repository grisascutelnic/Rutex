package com.scutelnic.rutex.dto;

public class ReactionRequest {
    private Long messageId;
    private String emoji;

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}
