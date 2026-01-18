package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Translation;
import com.scutelnic.rutex.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class TranslationService {
    
    @Autowired
    private TranslationRepository translationRepository;
    
    @Value("${translation.api.url:https://api.mymemory.translated.net/get}")
    private String translationApiUrl;
    
    @Value("${translation.api.key:}")
    private String translationApiKey;
    
    private final RestTemplate restTemplate = new RestTemplate();
    
    // Cache pentru traduceri în memorie
    private final Map<String, Map<String, String>> translationCache = new ConcurrentHashMap<>();
    
    /**
     * Obține traducerea pentru un text dat
     */
    public String getTranslation(String sourceText, String sourceLang, String targetLang, String pageName) {
        if (sourceText == null || sourceText.trim().isEmpty()) {
            return sourceText;
        }
        
        // Verificăm cache-ul în memorie
        String cacheKey = generateCacheKey(sourceLang, targetLang, pageName);
        Map<String, String> pageTranslations = translationCache.get(cacheKey);
        
        if (pageTranslations != null && pageTranslations.containsKey(sourceText)) {
            return pageTranslations.get(sourceText);
        }
        
        // Verificăm baza de date
        Optional<Translation> existingTranslation = translationRepository
            .findBySourceTextAndLanguages(sourceText, sourceLang, targetLang);
        
        if (existingTranslation.isPresent()) {
            Translation translation = existingTranslation.get();
            // Actualizăm cache-ul
            updateCache(cacheKey, sourceText, translation.getTranslatedText());
            return translation.getTranslatedText();
        }
        
        // Dacă nu există, facem traducerea prin API
        try {
            String translatedText = translateViaApi(sourceText, sourceLang, targetLang);
            
            // Salvăm în baza de date
            String translationKey = generateTranslationKey(sourceText, pageName);
            Translation newTranslation = new Translation(
                translationKey, sourceText, translatedText, sourceLang, targetLang, pageName
            );
            translationRepository.save(newTranslation);
            
            // Actualizăm cache-ul
            updateCache(cacheKey, sourceText, translatedText);
            
            return translatedText;
        } catch (Exception e) {
            System.err.println("Error translating text: " + sourceText + " - " + e.getMessage());
            return sourceText; // Returnăm textul original în caz de eroare
        }
    }
    
    /**
     * Traduce un text folosind API-ul extern
     */
    private String translateViaApi(String text, String sourceLang, String targetLang) {
        try {
            // Folosim MyMemory API (gratuit)
            String url = String.format("%s?q=%s&langpair=%s|%s", 
                translationApiUrl, 
                java.net.URLEncoder.encode(text, "UTF-8"),
                sourceLang, 
                targetLang);
            
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();
                
                if (responseBody.containsKey("responseData")) {
                    Map<String, Object> responseData = (Map<String, Object>) responseBody.get("responseData");
                    if (responseData.containsKey("translatedText")) {
                        return (String) responseData.get("translatedText");
                    }
                }
            }
            
            throw new RuntimeException("Invalid API response");
        } catch (Exception e) {
            throw new RuntimeException("Translation API error: " + e.getMessage(), e);
        }
    }
    
    /**
     * Încarcă toate traducerile pentru o pagină în cache
     */
    public void loadPageTranslations(String sourceLang, String targetLang, String pageName) {
        String cacheKey = generateCacheKey(sourceLang, targetLang, pageName);
        
        // Curățăm cache-ul pentru această cheie
        translationCache.remove(cacheKey);
        
        // Forțăm reîncărcarea din baza de date
        List<Translation> translations = translationRepository
            .findBySourceAndTargetLanguageAndPage(sourceLang, targetLang, pageName);
        
        Map<String, String> pageTranslations = translations.stream()
            .collect(Collectors.toMap(
                Translation::getSourceText,
                Translation::getTranslatedText
            ));
        
        translationCache.put(cacheKey, pageTranslations);
    }
    
    /**
     * Obține toate traducerile pentru o pagină
     */
    public Map<String, String> getPageTranslations(String sourceLang, String targetLang, String pageName) {
        loadPageTranslations(sourceLang, targetLang, pageName);
        String cacheKey = generateCacheKey(sourceLang, targetLang, pageName);
        return translationCache.getOrDefault(cacheKey, new HashMap<>());
    }
    
    /**
     * Generează o cheie de cache
     */
    private String generateCacheKey(String sourceLang, String targetLang, String pageName) {
        return sourceLang + "_" + targetLang + "_" + pageName;
    }
    
    /**
     * Generează o cheie de traducere
     */
    private String generateTranslationKey(String sourceText, String pageName) {
        return pageName + "_" + sourceText.hashCode();
    }
    
    /**
     * Actualizează cache-ul
     */
    private void updateCache(String cacheKey, String sourceText, String translatedText) {
        translationCache.computeIfAbsent(cacheKey, k -> new HashMap<>())
                       .put(sourceText, translatedText);
    }
    
    /**
     * Curăță cache-ul
     */
    public void clearCache() {
        translationCache.clear();
    }
    
    /**
     * Obține statistici despre traduceri
     */
    public Map<String, Object> getTranslationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTranslations", translationRepository.count());
        stats.put("roToRuTranslations", translationRepository.countByLanguages("ro", "ru"));
        stats.put("ruToRoTranslations", translationRepository.countByLanguages("ru", "ro"));
        stats.put("cachedPages", translationCache.size());
        return stats;
    }
    
    /**
     * Verifică dacă o traducere există
     */
    public boolean translationExists(String sourceText, String sourceLang, String targetLang) {
        return translationRepository.findBySourceTextAndLanguages(sourceText, sourceLang, targetLang).isPresent();
    }
}
