package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.LocalityDTO;
import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.repository.LocalityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import com.scutelnic.rutex.service.StatisticsService;

@Service
public class LocalityService {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalityService.class);
    private static final int MIN_LOCAL_RESULTS = 1; // Numărul minim de rezultate din baza locală înainte de a apela Google API
    
    // Mapare pentru diacriticele românești
    private static final Map<Character, Character> DIACRITICS_MAP = new HashMap<>();
    
    static {
        // Vocale cu diacritice românești
        DIACRITICS_MAP.put('ă', 'a');
        DIACRITICS_MAP.put('Ă', 'A');
        DIACRITICS_MAP.put('â', 'a');
        DIACRITICS_MAP.put('Â', 'A');
        DIACRITICS_MAP.put('î', 'i');
        DIACRITICS_MAP.put('Î', 'I');
        DIACRITICS_MAP.put('ș', 's');
        DIACRITICS_MAP.put('Ș', 'S');
        DIACRITICS_MAP.put('ț', 't');
        DIACRITICS_MAP.put('Ț', 'T');
        
        // Vocale cu diacritice internaționale (pentru suport complet)
        DIACRITICS_MAP.put('à', 'a');
        DIACRITICS_MAP.put('á', 'a');
        DIACRITICS_MAP.put('è', 'e');
        DIACRITICS_MAP.put('é', 'e');
        DIACRITICS_MAP.put('ì', 'i');
        DIACRITICS_MAP.put('í', 'i');
        DIACRITICS_MAP.put('ò', 'o');
        DIACRITICS_MAP.put('ó', 'o');
        DIACRITICS_MAP.put('ù', 'u');
        DIACRITICS_MAP.put('ú', 'u');
        DIACRITICS_MAP.put('ý', 'y');
        DIACRITICS_MAP.put('ñ', 'n');
        DIACRITICS_MAP.put('ç', 'c');
    }
    
    private final LocalityRepository localityRepository;
    private final GooglePlacesService googlePlacesService;
    private final StatisticsService statisticsService;
    
    public LocalityService(LocalityRepository localityRepository, GooglePlacesService googlePlacesService, StatisticsService statisticsService) {
        this.localityRepository = localityRepository;
        this.googlePlacesService = googlePlacesService;
        this.statisticsService = statisticsService;
    }
    
        public List<LocalityDTO> autocomplete(String query, String language, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        String trimmedQuery = query.trim();
        logger.info("🔍 AUTCOMPLETE: Query='{}', Language='{}', Limit={}", trimmedQuery, language, limit);

        // 1. CĂUTARE DIRECTĂ ÎN BAZA DE DATE
        List<Locality> localResults = findLocalitiesDirect(trimmedQuery, language, limit);
        logger.info("🔍 LOCAL SEARCH: Found {} results for '{}'", localResults.size(), trimmedQuery);
        
        // 2. Dacă găsesc rezultate locale, folosesc doar baza de date
        if (!localResults.isEmpty()) {
            logger.info("✅ USING LOCAL DATABASE: {} results for '{}'", localResults.size(), trimmedQuery);
            incrementLocalSearchCounters();
            
            return localResults.stream()
                    .map(LocalityDTO::new)
                    .collect(Collectors.toList());
        }

        // 3. Dacă nu găsesc nimic local, apelează Google Places API
        logger.info("❌ NO LOCAL RESULTS: Calling Google Places API for '{}'", trimmedQuery);
        incrementGoogleApiSearchCounters();

        List<Locality> googleResults = googlePlacesService.searchLocalities(trimmedQuery, language, limit);
        logger.info("🔍 GOOGLE RESULTS: Found {} results for '{}'", googleResults.size(), trimmedQuery);

        // 4. Verifică dacă rezultatele Google au fost deja salvate în baza de date
        List<Locality> existingGoogleResults = new ArrayList<>();
        for (Locality googleLocality : googleResults) {
            if (googleLocality.getGooglePlaceId() != null) {
                Optional<Locality> existing = localityRepository.findByGooglePlaceId(googleLocality.getGooglePlaceId());
                if (existing.isPresent()) {
                    existingGoogleResults.add(existing.get());
                    logger.info("🔍 FOUND EXISTING: Google Place ID '{}' already exists in database", googleLocality.getGooglePlaceId());
                }
            }
        }

        // 5. Dacă găsesc rezultate existente în baza de date, folosesc doar baza de date
        if (!existingGoogleResults.isEmpty()) {
            logger.info("✅ USING EXISTING GOOGLE RESULTS: {} results for '{}'", existingGoogleResults.size(), trimmedQuery);
            incrementLocalSearchCounters();
            
            return existingGoogleResults.stream()
                    .map(LocalityDTO::new)
                    .collect(Collectors.toList());
        }

        // 6. Dacă nu găsesc nimic existent, returnează rezultatele Google (care vor fi salvate)
        return googleResults.stream()
                .map(LocalityDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<LocalityDTO> searchLocalities(String query, String language, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }
        
        String trimmedQuery = query.trim();
        logger.info("Search request for query: '{}' in language: {}", trimmedQuery, language);
        
        // Caută în baza de date locală
        List<Locality> localResults = findLocalitiesDirect(trimmedQuery, language, limit);
        
        // LOGICA SIMPLĂ: Dacă găsesc ORICE rezultat local, folosesc doar baza de date
        if (!localResults.isEmpty()) {
            logger.info("✅ LOCAL SEARCH: Found {} results for '{}' - using LOCAL database only", localResults.size(), trimmedQuery);
            incrementLocalSearchCounters();
            
            // Limitează rezultatele locale
            if (localResults.size() > limit) {
                localResults = localResults.subList(0, limit);
            }
            
            return localResults.stream()
                    .map(LocalityDTO::new)
                    .collect(Collectors.toList());
        }
        
        // Dacă nu găsesc NIMIC local, apelează Google Places API
        logger.info("❌ LOCAL SEARCH: No results for '{}' - calling GOOGLE PLACES API", trimmedQuery);
        incrementGoogleApiSearchCounters();
        
        List<Locality> googleResults = googlePlacesService.searchLocalities(trimmedQuery, language, limit);
        
        // Verifică dacă rezultatele Google au fost deja salvate în baza de date
        List<Locality> existingGoogleResults = new ArrayList<>();
        for (Locality googleLocality : googleResults) {
            if (googleLocality.getGooglePlaceId() != null) {
                Optional<Locality> existing = localityRepository.findByGooglePlaceId(googleLocality.getGooglePlaceId());
                if (existing.isPresent()) {
                    existingGoogleResults.add(existing.get());
                    logger.info("🔍 FOUND EXISTING: Google Place ID '{}' already exists in database", googleLocality.getGooglePlaceId());
                }
            }
        }

        // Dacă găsesc rezultate existente în baza de date, folosesc doar baza de date
        if (!existingGoogleResults.isEmpty()) {
            logger.info("✅ USING EXISTING GOOGLE RESULTS: {} results for '{}'", existingGoogleResults.size(), trimmedQuery);
            incrementLocalSearchCounters();
            
            // Limitează rezultatele
            if (existingGoogleResults.size() > limit) {
                existingGoogleResults = existingGoogleResults.subList(0, limit);
            }
            
            return existingGoogleResults.stream()
                    .map(LocalityDTO::new)
                    .collect(Collectors.toList());
        }
        
        return googleResults.stream()
                .map(LocalityDTO::new)
                .collect(Collectors.toList());
    }
    
    public Optional<LocalityDTO> getLocalityById(Long id) {
        return localityRepository.findById(id)
                .map(LocalityDTO::new);
    }
    
    public Optional<LocalityDTO> getLocalityByGooglePlaceId(String googlePlaceId) {
        return localityRepository.findByGooglePlaceId(googlePlaceId)
                .map(LocalityDTO::new);
    }
    
    public List<LocalityDTO> getLocalitiesByDistrict(Long districtId) {
        return localityRepository.findByDistrictId(districtId)
                .stream()
                .map(LocalityDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<LocalityDTO> getLocalitiesByType(Locality.LocalityType type) {
        return localityRepository.findByType(type)
                .stream()
                .map(LocalityDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<LocalityDTO> getMostPopularLocalities(int limit) {
        Page<Locality> popularLocalities = localityRepository.findMostPopular(PageRequest.of(0, limit));
        return popularLocalities.getContent()
                .stream()
                .map(LocalityDTO::new)
                .collect(Collectors.toList());
    }
    
    public List<LocalityDTO> getLocalitiesInBoundingBox(double minLat, double maxLat, double minLng, double maxLng) {
        return localityRepository.findByBoundingBox(minLat, maxLat, minLng, maxLng)
                .stream()
                .map(LocalityDTO::new)
                .collect(Collectors.toList());
    }
    
    public void incrementSearchCount(Long localityId) {
        localityRepository.findById(localityId).ifPresent(locality -> {
            locality.incrementSearchCount();
            localityRepository.save(locality);
        });
    }
    
    public long getTotalLocalitiesCount() {
        return localityRepository.count();
    }
    
    public long getLocalitiesCountByDistrict(Long districtId) {
        return localityRepository.countByDistrictId(districtId);
    }
    
    private List<Locality> combineResults(List<Locality> localResults, List<Locality> googleResults) {
        // Creează o listă combinată, evitând duplicatelor
        List<Locality> combined = new ArrayList<>(localResults);
        
        for (Locality googleLocality : googleResults) {
            boolean isDuplicate = localResults.stream()
                    .anyMatch(local -> local.getGooglePlaceId() != null && 
                                     local.getGooglePlaceId().equals(googleLocality.getGooglePlaceId()));
            
            if (!isDuplicate) {
                combined.add(googleLocality);
            }
        }
        
        return combined;
    }
    
    /**
     * CĂUTARE SIMPLĂ ÎN BAZA DE DATE LOCALĂ
     * Caută direct în baza de date fără normalizări complicate
     */
    private List<Locality> findLocalitiesDirect(String query, String language, int limit) {
        logger.info("🔍 DIRECT SEARCH: Query='{}', Language='{}'", query, language);
        
        // Folosește query-uri directe în baza de date pentru performanță mai bună
        List<Locality> results;
        
        if ("ru".equals(language)) {
            // Căutare în limba rusă
            results = localityRepository.findByNameRuContainingIgnoreCase(query);
        } else {
            // Căutare în limba română
            results = localityRepository.findByNameRoContainingIgnoreCase(query);
        }
        
        logger.info("🔍 DIRECT QUERY RESULTS: Found {} results for '{}'", results.size(), query);
        
        // Dacă nu găsesc rezultate cu căutarea exactă, încearcă cu normalizarea diacriticelor
        if (results.isEmpty()) {
            String normalizedQuery = normalizeRomanianDiacritics(query.toLowerCase());
            logger.info("🔍 TRYING NORMALIZED SEARCH: '{}' -> '{}'", query, normalizedQuery);
            
            // Caută în toate localitățile cu normalizare
            List<Locality> allLocalities = localityRepository.findAllWithDistrict();
            results = allLocalities.stream()
                    .filter(locality -> {
                        String localityName = "ru".equals(language) ? locality.getNameRu() : locality.getNameRo();
                        if (localityName == null) return false;
                        
                        String normalizedLocalityName = normalizeRomanianDiacritics(localityName.toLowerCase());
                        return normalizedLocalityName.contains(normalizedQuery);
                    })
                    .collect(Collectors.toList());
            
            logger.info("🔍 NORMALIZED SEARCH RESULTS: Found {} results for '{}'", results.size(), normalizedQuery);
        }
        
        // Dacă încă nu găsesc rezultate, încearcă căutarea cu cuvinte individuale
        if (results.isEmpty() && query.contains(" ")) {
            logger.info("🔍 TRYING WORD-BASED SEARCH for multi-word query: '{}'", query);
            
            // Împarte query-ul în cuvinte
            String[] words = query.toLowerCase().split("\\s+");
            String normalizedQuery = normalizeRomanianDiacritics(query.toLowerCase());
            String[] normalizedWords = normalizedQuery.split("\\s+");
            
            // Caută în toate localitățile
            List<Locality> allLocalities = localityRepository.findAllWithDistrict();
            results = allLocalities.stream()
                    .filter(locality -> {
                        String localityName = "ru".equals(language) ? locality.getNameRu() : locality.getNameRo();
                        if (localityName == null) return false;
                        
                        String normalizedLocalityName = normalizeRomanianDiacritics(localityName.toLowerCase());
                        
                        // Obține numele districtului
                        String districtName = "";
                        if (locality.getDistrict() != null) {
                            districtName = "ru".equals(language) ? locality.getDistrict().getNameRu() : locality.getDistrict().getNameRo();
                            if (districtName == null) districtName = "";
                        }
                        String normalizedDistrictName = normalizeRomanianDiacritics(districtName.toLowerCase());
                        
                        // Combină numele localității și districtului pentru căutare
                        String combinedName = localityName.toLowerCase() + " " + districtName.toLowerCase();
                        String normalizedCombinedName = normalizedLocalityName + " " + normalizedDistrictName;
                        
                        // Verifică dacă toate cuvintele din query sunt prezente în combinația localitate + district
                        boolean allWordsMatch = true;
                        for (String word : normalizedWords) {
                            if (word.length() > 2 && 
                                !normalizedLocalityName.contains(word) && 
                                !normalizedDistrictName.contains(word) &&
                                !normalizedCombinedName.contains(word)) {
                                allWordsMatch = false;
                                break;
                            }
                        }
                        
                        // Verifică și cuvintele originale (pentru cazuri precum "moldova")
                        if (!allWordsMatch) {
                            allWordsMatch = true;
                            for (String word : words) {
                                if (word.length() > 2 && 
                                    !localityName.toLowerCase().contains(word) && 
                                    !districtName.toLowerCase().contains(word) &&
                                    !combinedName.contains(word)) {
                                    allWordsMatch = false;
                                    break;
                                }
                            }
                        }
                        
                        return allWordsMatch;
                    })
                    .collect(Collectors.toList());
            
            logger.info("🔍 WORD-BASED SEARCH RESULTS: Found {} results for '{}'", results.size(), query);
        }
        
        // Sortare și limitare
        results = results.stream()
                .sorted((a, b) -> {
                    String aName = "ru".equals(language) ? a.getNameRu() : a.getNameRo();
                    String bName = "ru".equals(language) ? b.getNameRu() : b.getNameRo();
                    
                    String normalizedAName = normalizeRomanianDiacritics(aName.toLowerCase());
                    String normalizedBName = normalizeRomanianDiacritics(bName.toLowerCase());
                    String normalizedQuery = normalizeRomanianDiacritics(query.toLowerCase());
                    
                    // 1. Prioritize exact matches (starts with query)
                    boolean aStartsWith = aName.toLowerCase().startsWith(query.toLowerCase()) || 
                                        normalizedAName.startsWith(normalizedQuery);
                    boolean bStartsWith = bName.toLowerCase().startsWith(query.toLowerCase()) || 
                                        normalizedBName.startsWith(normalizedQuery);
                    
                    if (aStartsWith && !bStartsWith) return -1;
                    if (!aStartsWith && bStartsWith) return 1;
                    
                    // 2. Prioritize Moldova
                    String aCountry = a.getCountryCode();
                    String bCountry = b.getCountryCode();
                    
                    if ("MD".equals(aCountry) && !"MD".equals(bCountry)) return -1;
                    if (!"MD".equals(aCountry) && "MD".equals(bCountry)) return 1;
                    
                    // 3. Sort by popularity
                    int aCount = a.getSearchCount() != null ? a.getSearchCount() : 0;
                    int bCount = b.getSearchCount() != null ? b.getSearchCount() : 0;
                    return Integer.compare(bCount, aCount);
                })
                .limit(limit)
                .collect(Collectors.toList());
        
        logger.info("🔍 FINAL DIRECT RESULTS: Found {} results for '{}'", results.size(), query);
        
        // DEBUG: Afișează primele 3 rezultate găsite
        if (results.size() > 0) {
            logger.info("🔍 FIRST RESULT: {}", 
                "ru".equals(language) ? results.get(0).getNameRu() : results.get(0).getNameRo());
        }
        
        return results;
    }
    
    /**
     * Normalizează un string prin eliminarea diacriticelor românești
     * Exemplu: "Vărzărești" → "Varzaresti"
     * Suportă toate diacriticele: ă, â, î, ș, ț, Ă, Â, Î, Ș, Ț
     */
    private String normalizeRomanianDiacritics(String input) {
        if (input == null) return "";
        
        StringBuilder result = new StringBuilder();
        
        for (char c : input.toCharArray()) {
            // Verifică dacă caracterul este o diacritică românească
            Character replacement = DIACRITICS_MAP.get(c);
            if (replacement != null) {
                result.append(replacement);
            } else {
                result.append(c);
            }
        }
        
        // Elimină caracterele speciale și păstrează doar litere, cifre și spații
        return result.toString().replaceAll("[^\\p{L}\\p{N}\\s]", "").trim();
    }

    /**
     * Increment local search counters
     */
    public void incrementLocalSearchCounters() {
        try {
            if (statisticsService == null) {
                logger.error("StatisticsService is null!");
                return;
            }
            statisticsService.resetDailyCountersIfNeeded();
            statisticsService.incrementStat("total_local_searches");
            statisticsService.incrementStat("today_local_searches");
            statisticsService.incrementStat("week_local_searches");
            statisticsService.incrementStat("month_local_searches");
        } catch (Exception e) {
            logger.error("Error incrementing local search counters", e);
        }
    }
    
    /**
     * Increment Google API search counters
     */
    public void incrementGoogleApiSearchCounters() {
        try {
            if (statisticsService == null) {
                logger.error("StatisticsService is null!");
                return;
            }
            statisticsService.resetDailyCountersIfNeeded();
            statisticsService.incrementStat("total_google_api_searches");
            statisticsService.incrementStat("today_google_api_searches");
            statisticsService.incrementStat("week_google_api_searches");
            statisticsService.incrementStat("month_google_api_searches");
        } catch (Exception e) {
            logger.error("Error incrementing Google API search counters", e);
        }
    }
    
    /**
     * Get locality search statistics (local vs Google API)
     */
    public Map<String, Object> getSearchStatistics() {
        return statisticsService.getSearchStatistics();
    }
    
    /**
     * Reset all search counters (for testing purposes)
     */
    public void resetSearchCounters() {
        statisticsService.resetAllCounters();
    }
    
    /**
     * Get StatisticsService for debugging purposes
     */
    public StatisticsService getStatisticsService() {
        return statisticsService;
    }
}
