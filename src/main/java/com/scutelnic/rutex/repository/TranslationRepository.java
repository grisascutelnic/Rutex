package com.scutelnic.rutex.repository;

import com.scutelnic.rutex.entity.Translation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TranslationRepository extends JpaRepository<Translation, Long> {
    
    @Query("SELECT t FROM Translation t WHERE t.sourceLanguage = :sourceLang AND t.targetLanguage = :targetLang AND t.isActive = true")
    List<Translation> findBySourceAndTargetLanguage(@Param("sourceLang") String sourceLang, @Param("targetLang") String targetLang);
    
    @Query("SELECT t FROM Translation t WHERE t.sourceLanguage = :sourceLang AND t.targetLanguage = :targetLang AND t.pageName = :pageName AND t.isActive = true")
    List<Translation> findBySourceAndTargetLanguageAndPage(@Param("sourceLang") String sourceLang, 
                                                          @Param("targetLang") String targetLang, 
                                                          @Param("pageName") String pageName);
    
    @Query("SELECT t FROM Translation t WHERE t.translationKey = :key AND t.sourceLanguage = :sourceLang AND t.targetLanguage = :targetLang AND t.isActive = true")
    Optional<Translation> findByKeyAndLanguages(@Param("key") String key, 
                                               @Param("sourceLang") String sourceLang, 
                                               @Param("targetLang") String targetLang);
    
    @Query("SELECT t FROM Translation t WHERE t.sourceText = :sourceText AND t.sourceLanguage = :sourceLang AND t.targetLanguage = :targetLang AND t.isActive = true")
    Optional<Translation> findBySourceTextAndLanguages(@Param("sourceText") String sourceText, 
                                                      @Param("sourceLang") String sourceLang, 
                                                      @Param("targetLang") String targetLang);
    
    @Query("SELECT DISTINCT t.pageName FROM Translation t WHERE t.sourceLanguage = :sourceLang AND t.targetLanguage = :targetLang AND t.isActive = true")
    List<String> findDistinctPagesByLanguages(@Param("sourceLang") String sourceLang, @Param("targetLang") String targetLang);
    
    @Query("SELECT COUNT(t) FROM Translation t WHERE t.sourceLanguage = :sourceLang AND t.targetLanguage = :targetLang AND t.isActive = true")
    long countByLanguages(@Param("sourceLang") String sourceLang, @Param("targetLang") String targetLang);
}
