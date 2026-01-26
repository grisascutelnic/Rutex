package com.scutelnic.rutex.dto;

public class MessageReadRequest {
    private Long conversationId;
    private Long lastMessageId;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getLastMessageId() { return lastMessageId; }
    public void setLastMessageId(Long lastMessageId) { this.lastMessageId = lastMessageId; }
}
