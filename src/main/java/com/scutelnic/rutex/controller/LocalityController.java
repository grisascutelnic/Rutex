package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.LocalityDTO;
import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.service.LocalityService;
import com.scutelnic.rutex.repository.LocalityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/localities")
@CrossOrigin(origins = "*")
public class LocalityController {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalityController.class);
    
    private final LocalityService localityService;
    private final LocalityRepository localityRepository;
    
    public LocalityController(LocalityService localityService, LocalityRepository localityRepository) {
        this.localityService = localityService;
        this.localityRepository = localityRepository;
    }
    
    @GetMapping("/autocomplete")
    public ResponseEntity<List<LocalityDTO>> autocomplete(
            @RequestParam String query,
            @RequestParam(defaultValue = "ro") String language,
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Autocomplete request: query='{}', language='{}', limit={}", query, language, limit);

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        
        List<LocalityDTO> results = localityService.autocomplete(query, language, limit);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/search")
    public ResponseEntity<List<LocalityDTO>> searchLocalities(
            @RequestParam String query,
            @RequestParam(defaultValue = "ro") String language,
            @RequestParam(defaultValue = "20") int limit) {
        
        logger.info("Search request: query='{}', language='{}', limit={}", query, language, limit);

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }
        
        List<LocalityDTO> results = localityService.searchLocalities(query, language, limit);
        return ResponseEntity.ok(results);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<LocalityDTO> getLocalityById(@PathVariable Long id) {
        logger.info("Get locality by ID: {}", id);

        Optional<LocalityDTO> locality = localityService.getLocalityById(id);
        return locality.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/google-place/{googlePlaceId}")
    public ResponseEntity<LocalityDTO> getLocalityByGooglePlaceId(@PathVariable String googlePlaceId) {
        logger.info("Get locality by Google Place ID: {}", googlePlaceId);

        Optional<LocalityDTO> locality = localityService.getLocalityByGooglePlaceId(googlePlaceId);
        return locality.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/district/{districtId}")
    public ResponseEntity<List<LocalityDTO>> getLocalitiesByDistrict(@PathVariable Long districtId) {
        logger.info("Get localities by district ID: {}", districtId);

        List<LocalityDTO> localities = localityService.getLocalitiesByDistrict(districtId);
        return ResponseEntity.ok(localities);
    }
    
    @GetMapping("/type/{type}")
    public ResponseEntity<List<LocalityDTO>> getLocalitiesByType(@PathVariable String type) {
        logger.info("Get localities by type: {}", type);

        try {
            Locality.LocalityType localityType = Locality.LocalityType.valueOf(type.toUpperCase());
            List<LocalityDTO> localities = localityService.getLocalitiesByType(localityType);
            return ResponseEntity.ok(localities);
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid locality type: {}", type);
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/popular")
    public ResponseEntity<List<LocalityDTO>> getMostPopularLocalities(
            @RequestParam(defaultValue = "10") int limit) {
        
        logger.info("Get most popular localities, limit: {}", limit);

        List<LocalityDTO> localities = localityService.getMostPopularLocalities(limit);
        return ResponseEntity.ok(localities);
    }
    
    @GetMapping("/bounding-box")
    public ResponseEntity<List<LocalityDTO>> getLocalitiesInBoundingBox(
            @RequestParam double minLat,
            @RequestParam double maxLat,
            @RequestParam double minLng,
            @RequestParam double maxLng) {
        
        logger.info("Get localities in bounding box: ({}, {}) to ({}, {})", minLat, minLng, maxLat, maxLng);

        List<LocalityDTO> localities = localityService.getLocalitiesInBoundingBox(minLat, maxLat, minLng, maxLng);
        return ResponseEntity.ok(localities);
    }
    
    @PostMapping("/{id}/increment-search")
    public ResponseEntity<Void> incrementSearchCount(@PathVariable Long id) {
        logger.info("Increment search count for locality ID: {}", id);

        // Increment locality search count
        localityService.incrementSearchCount(id);
        
        // Also increment general statistics - this is called when user selects from autocomplete
        // We'll increment local search counters since this is typically from local database results
        localityService.incrementLocalSearchCounters();
        
        return ResponseEntity.ok().build();
    }
    

    

    
    @PostMapping("/{id}/increment-search-google")
    public ResponseEntity<Void> incrementSearchCountGoogle(@PathVariable Long id) {
        logger.info("Increment search count for Google API locality ID: {}", id);

        // Increment locality search count
        localityService.incrementSearchCount(id);
        
        // Increment Google API search counters since this came from Google Places API
        localityService.incrementGoogleApiSearchCounters();
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stats/total")
    public ResponseEntity<Long> getTotalLocalitiesCount() {
        long count = localityService.getTotalLocalitiesCount();
        return ResponseEntity.ok(count);
    }
    
    @GetMapping("/stats/district/{districtId}")
    public ResponseEntity<Long> getLocalitiesCountByDistrict(@PathVariable Long districtId) {
        long count = localityService.getLocalitiesCountByDistrict(districtId);
        return ResponseEntity.ok(count);
    }
    
    // Endpoint pentru testare - returnează toate localitățile (cu paginare)
    @GetMapping("/all")
    public ResponseEntity<List<LocalityDTO>> getAllLocalities(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        
        logger.info("Get all localities: page={}, size={}", page, size);
        
        // Aceasta este o implementare simplă - în producție ar trebui să folosești paginare
        List<LocalityDTO> localities = localityService.getMostPopularLocalities(size);
        return ResponseEntity.ok(localities);
    }
    
    @GetMapping("/debug/database")
    public ResponseEntity<Map<String, Object>> debugDatabase() {
        try {
            Map<String, Object> result = new HashMap<>();
            
            // Obține toate localitățile
            List<Locality> allLocalities = localityRepository.findAllWithDistrict();
            result.put("totalLocalities", allLocalities.size());
            
            // Caută localități care conțin "edine"
            List<Map<String, String>> edineLocalities = allLocalities.stream()
                    .filter(loc -> loc.getNameRo() != null && loc.getNameRo().toLowerCase().contains("edine"))
                    .map(loc -> {
                        Map<String, String> locMap = new HashMap<>();
                        locMap.put("id", loc.getId().toString());
                        locMap.put("nameRo", loc.getNameRo());
                        locMap.put("nameRu", loc.getNameRu());
                        return locMap;
                    })
                    .collect(Collectors.toList());
            result.put("edineLocalities", edineLocalities);
            
            // Caută localități care conțin "dubas"
            List<Map<String, String>> dubasLocalities = allLocalities.stream()
                    .filter(loc -> loc.getNameRo() != null && loc.getNameRo().toLowerCase().contains("dubas"))
                    .map(loc -> {
                        Map<String, String> locMap = new HashMap<>();
                        locMap.put("id", loc.getId().toString());
                        locMap.put("nameRo", loc.getNameRo());
                        locMap.put("nameRu", loc.getNameRu());
                        return locMap;
                    })
                    .collect(Collectors.toList());
            result.put("dubasLocalities", dubasLocalities);
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            logger.error("Error in debug database", e);
            Map<String, Object> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

}
