package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.TranslationService;
import com.scutelnic.rutex.entity.Translation;
import com.scutelnic.rutex.repository.TranslationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/translations")
public class TranslationAdminController {
    
    @Autowired
    private TranslationService translationService;
    
    @Autowired
    private TranslationRepository translationRepository;
    
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getTranslationStats() {
        return ResponseEntity.ok(translationService.getTranslationStats());
    }
    
    @GetMapping("/page/{pageName}")
    public ResponseEntity<Map<String, String>> getPageTranslations(
            @PathVariable String pageName,
            @RequestParam String sourceLang,
            @RequestParam String targetLang) {
        return ResponseEntity.ok(translationService.getPageTranslations(sourceLang, targetLang, pageName));
    }
    
    @PostMapping("/translate")
    public ResponseEntity<Map<String, String>> translateText(
            @RequestParam String text,
            @RequestParam String sourceLang,
            @RequestParam String targetLang,
            @RequestParam String pageName) {
        String translatedText = translationService.getTranslation(text, sourceLang, targetLang, pageName);
        return ResponseEntity.ok(Map.of("translatedText", translatedText));
    }
    
    @GetMapping("/all")
    public ResponseEntity<List<Translation>> getAllTranslations() {
        return ResponseEntity.ok(translationRepository.findAll());
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTranslation(@PathVariable Long id) {
        translationRepository.deleteById(id);
        translationService.clearCache();
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<Translation> updateTranslation(
            @PathVariable Long id,
            @RequestBody Translation translation) {
        if (translationRepository.existsById(id)) {
            translation.setId(id);
            Translation saved = translationRepository.save(translation);
            translationService.clearCache();
            return ResponseEntity.ok(saved);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/clear-cache")
    public ResponseEntity<Void> clearCache() {
        translationService.clearCache();
        return ResponseEntity.ok().build();
    }
}
