package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "translations")
public class Translation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "translation_key", nullable = false, length = 500)
    private String translationKey;
    
    @Column(name = "source_text", nullable = false, columnDefinition = "TEXT")
    private String sourceText;
    
    @Column(name = "translated_text", nullable = false, columnDefinition = "TEXT")
    private String translatedText;
    
    @Column(name = "source_language", nullable = false, length = 10)
    private String sourceLanguage;
    
    @Column(name = "target_language", nullable = false, length = 10)
    private String targetLanguage;
    
    @Column(name = "page_name", length = 100)
    private String pageName;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    // Constructors
    public Translation() {
        this.createdAt = LocalDateTime.now();
    }
    
    public Translation(String translationKey, String sourceText, String translatedText, 
                      String sourceLanguage, String targetLanguage, String pageName) {
        this();
        this.translationKey = translationKey;
        this.sourceText = sourceText;
        this.translatedText = translatedText;
        this.sourceLanguage = sourceLanguage;
        this.targetLanguage = targetLanguage;
        this.pageName = pageName;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTranslationKey() {
        return translationKey;
    }
    
    public void setTranslationKey(String translationKey) {
        this.translationKey = translationKey;
    }
    
    public String getSourceText() {
        return sourceText;
    }
    
    public void setSourceText(String sourceText) {
        this.sourceText = sourceText;
    }
    
    public String getTranslatedText() {
        return translatedText;
    }
    
    public void setTranslatedText(String translatedText) {
        this.translatedText = translatedText;
    }
    
    public String getSourceLanguage() {
        return sourceLanguage;
    }
    
    public void setSourceLanguage(String sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }
    
    public String getTargetLanguage() {
        return targetLanguage;
    }
    
    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
    
    public String getPageName() {
        return pageName;
    }
    
    public void setPageName(String pageName) {
        this.pageName = pageName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public boolean isActive() {
        return isActive;
    }
    
    public void setActive(boolean active) {
        isActive = active;
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
