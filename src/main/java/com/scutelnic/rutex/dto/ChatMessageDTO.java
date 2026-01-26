package com.scutelnic.rutex.dto;

import java.time.LocalDateTime;
import java.util.List;

public class ChatMessageDTO {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String senderProfileImage;
    private String contentText;
    private String imageUrl;
    private LocalDateTime createdAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime readAt;
    private boolean own;
    private List<ReactionSummaryDTO> reactions;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }
    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }
    public String getSenderProfileImage() { return senderProfileImage; }
    public void setSenderProfileImage(String senderProfileImage) { this.senderProfileImage = senderProfileImage; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(LocalDateTime deliveredAt) { this.deliveredAt = deliveredAt; }
    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }
    public boolean isOwn() { return own; }
    public void setOwn(boolean own) { this.own = own; }
    public List<ReactionSummaryDTO> getReactions() { return reactions; }
    public void setReactions(List<ReactionSummaryDTO> reactions) { this.reactions = reactions; }
}
