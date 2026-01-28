package com.scutelnic.rutex.dto;

import java.time.LocalDateTime;

public class ConversationDTO {
    private Long conversationId;
    private Long otherUserId;
    private String otherUserName;
    private String otherUserProfileImage;
    private String lastMessageText;
    private String lastMessageImageUrl;
    private LocalDateTime lastMessageAt;
    private Long lastMessageSenderId;
    private String lastMessageStatus;
    private long unreadCount;
    private boolean otherUserOnline;
    private LocalDateTime otherUserLastSeenAt;

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getOtherUserId() { return otherUserId; }
    public void setOtherUserId(Long otherUserId) { this.otherUserId = otherUserId; }
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }
    public String getOtherUserProfileImage() { return otherUserProfileImage; }
    public void setOtherUserProfileImage(String otherUserProfileImage) { this.otherUserProfileImage = otherUserProfileImage; }
    public String getLastMessageText() { return lastMessageText; }
    public void setLastMessageText(String lastMessageText) { this.lastMessageText = lastMessageText; }
    public String getLastMessageImageUrl() { return lastMessageImageUrl; }
    public void setLastMessageImageUrl(String lastMessageImageUrl) { this.lastMessageImageUrl = lastMessageImageUrl; }
    public LocalDateTime getLastMessageAt() { return lastMessageAt; }
    public void setLastMessageAt(LocalDateTime lastMessageAt) { this.lastMessageAt = lastMessageAt; }
    public Long getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(Long lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }
    public String getLastMessageStatus() { return lastMessageStatus; }
    public void setLastMessageStatus(String lastMessageStatus) { this.lastMessageStatus = lastMessageStatus; }
    public long getUnreadCount() { return unreadCount; }
    public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
    public boolean isOtherUserOnline() { return otherUserOnline; }
    public void setOtherUserOnline(boolean otherUserOnline) { this.otherUserOnline = otherUserOnline; }
    public LocalDateTime getOtherUserLastSeenAt() { return otherUserLastSeenAt; }
    public void setOtherUserLastSeenAt(LocalDateTime otherUserLastSeenAt) { this.otherUserLastSeenAt = otherUserLastSeenAt; }
}
