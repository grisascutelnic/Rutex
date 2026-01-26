package com.scutelnic.rutex.dto;

public class ReactionSummaryDTO {
    private String emoji;
    private int count;
    private boolean reacted;

    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public boolean isReacted() { return reacted; }
    public void setReacted(boolean reacted) { this.reacted = reacted; }
}
