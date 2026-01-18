package com.scutelnic.rutex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scutelnic.rutex.entity.District;
import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.repository.DistrictRepository;
import com.scutelnic.rutex.repository.LocalityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.beans.factory.annotation.Autowired;
import com.scutelnic.rutex.service.StatisticsService;

@Service
public class GooglePlacesService {
    
    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesService.class);
    
    @Value("${google.places.api.key}")
    private String apiKey;
    
    @Value("${google.places.api.base-url:https://maps.googleapis.com/maps/api/place}")
    private String baseUrl;
    
    // Moldova bounding box coordinates
    private static final double MOLDOVA_MIN_LAT = 45.47;
    private static final double MOLDOVA_MAX_LAT = 48.47;
    private static final double MOLDOVA_MIN_LNG = 26.62;
    private static final double MOLDOVA_MAX_LNG = 30.14;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final LocalityRepository localityRepository;
    private final DistrictRepository districtRepository;
    private final StatisticsService statisticsService;
    
    public GooglePlacesService(RestTemplate restTemplate, ObjectMapper objectMapper,
                             LocalityRepository localityRepository, DistrictRepository districtRepository,
                             StatisticsService statisticsService) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.localityRepository = localityRepository;
        this.districtRepository = districtRepository;
        this.statisticsService = statisticsService;
    }
    
    public List<Locality> searchLocalities(String query, String language, int limit) {
        List<Locality> results = new ArrayList<>();
        
        try {
            // Increment API call counters
            incrementApiCallCounters();
            
            // Construiește URL-ul pentru Places API Autocomplete
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/autocomplete/json")
                    .queryParam("input", query)
                    .queryParam("types", "(cities)")
                    // Removed country restriction: .queryParam("components", "country:md")
                    .queryParam("language", language)
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();
            
            logger.info("Calling Google Places API: {}", url);
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if ("OK".equals(root.path("status").asText())) {
                JsonNode predictions = root.path("predictions");
                
                for (JsonNode prediction : predictions) {
                    if (results.size() >= limit) break;
                    
                    String placeId = prediction.path("place_id").asText();
                    String description = prediction.path("description").asText();
                    
                    // Obține detalii complete pentru locație
                    Locality locality = getPlaceDetails(placeId, language);
                    if (locality != null) {
                        results.add(locality);
                    }
                }
            } else {
                logger.warn("Google Places API returned status: {}", root.path("status").asText());
            }
            
        } catch (Exception e) {
            logger.error("Error calling Google Places API", e);
        }
        
        return results;
    }
    
    private Locality getPlaceDetails(String placeId, String language) {
        try {
            // Verifică dacă localitatea există deja în baza de date
            Optional<Locality> existingLocality = localityRepository.findByGooglePlaceId(placeId);
            if (existingLocality.isPresent()) {
                Locality locality = existingLocality.get();
                locality.incrementSearchCount();
                return localityRepository.save(locality);
            }
            
            // Construiește URL-ul pentru Place Details
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/details/json")
                    .queryParam("place_id", placeId)
                    .queryParam("fields", "name,geometry,types,address_components")
                    .queryParam("language", language)
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if ("OK".equals(root.path("status").asText())) {
                JsonNode result = root.path("result");
                
                String name = result.path("name").asText();
                JsonNode geometry = result.path("geometry");
                JsonNode location = geometry.path("location");
                
                double lat = location.path("lat").asDouble();
                double lng = location.path("lng").asDouble();
                
                // Removed Moldova bounding box check - now accepting global locations
                // if (!isInMoldova(lat, lng)) {
                //     logger.info("Location {} ({}, {}) is outside Moldova bounds", name, lat, lng);
                //     return null;
                // }
                
                // Determină tipul localității
                Locality.LocalityType type = determineLocalityType(result.path("types"));
                
                // Obține districtul
                District district = extractDistrict(result.path("address_components"));
                
                // Extrage informații despre țară
                String countryCode = extractCountryCode(result.path("address_components"));
                String countryName = extractCountryName(result.path("address_components"));
                
                // Creează localitatea
                Locality locality = new Locality();
                locality.setGooglePlaceId(placeId);
                locality.setLatitude(lat);
                locality.setLongitude(lng);
                locality.setType(type);
                locality.setDistrict(district);
                locality.setSearchCount(1);
                locality.setCountryCode(countryCode);
                locality.setCountryNameRo(countryName);
                locality.setCountryNameRu(countryName); // Va fi actualizat mai târziu
                
                // Setează numele în funcție de limbă
                if ("ru".equals(language)) {
                    locality.setNameRu(name);
                    // Încearcă să obțină numele în română
                    String romanianName = getRomanianName(placeId);
                    locality.setNameRo(romanianName != null ? romanianName : name);
                } else {
                    locality.setNameRo(name);
                    // Încearcă să obțină numele în rusă
                    String russianName = getRussianName(placeId);
                    locality.setNameRu(russianName != null ? russianName : name);
                }
                
                return localityRepository.save(locality);
            }
            
        } catch (Exception e) {
            logger.error("Error getting place details for placeId: {}", placeId, e);
        }
        
        return null;
    }
    
    private boolean isInMoldova(double lat, double lng) {
        return lat >= MOLDOVA_MIN_LAT && lat <= MOLDOVA_MAX_LAT &&
               lng >= MOLDOVA_MIN_LNG && lng <= MOLDOVA_MAX_LNG;
    }
    
    private Locality.LocalityType determineLocalityType(JsonNode types) {
        for (JsonNode type : types) {
            String typeStr = type.asText();
            switch (typeStr) {
                case "locality":
                case "political":
                    return Locality.LocalityType.CITY;
                case "sublocality":
                case "sublocality_level_1":
                    return Locality.LocalityType.TOWN;
                case "administrative_area_level_2":
                    return Locality.LocalityType.MUNICIPALITY;
                default:
                    break;
            }
        }
        return Locality.LocalityType.VILLAGE;
    }
    
    private District extractDistrict(JsonNode addressComponents) {
        for (JsonNode component : addressComponents) {
            JsonNode types = component.path("types");
            for (JsonNode type : types) {
                if ("administrative_area_level_1".equals(type.asText())) {
                    String districtName = component.path("long_name").asText();
                    String shortName = component.path("short_name").asText();
                    
                    // Caută districtul în baza de date
                    Optional<District> existingDistrict = districtRepository.findByNameContainingIgnoreCase(districtName)
                            .stream()
                            .findFirst();
                    
                    if (existingDistrict.isPresent()) {
                        return existingDistrict.get();
                    } else {
                        // Creează un district nou
                        District newDistrict = new District();
                        newDistrict.setNameRo(districtName);
                        newDistrict.setNameRu(districtName); // Va fi actualizat mai târziu
                        return districtRepository.save(newDistrict);
                    }
                }
            }
        }
        return null;
    }
    
    private String getRomanianName(String placeId) {
        return getPlaceNameInLanguage(placeId, "ro");
    }
    
    private String getRussianName(String placeId) {
        return getPlaceNameInLanguage(placeId, "ru");
    }
    
    private String getPlaceNameInLanguage(String placeId, String language) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(baseUrl + "/details/json")
                    .queryParam("place_id", placeId)
                    .queryParam("fields", "name")
                    .queryParam("language", language)
                    .queryParam("key", apiKey)
                    .build()
                    .toUriString();
            
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            JsonNode root = objectMapper.readTree(response.getBody());
            
            if ("OK".equals(root.path("status").asText())) {
                return root.path("result").path("name").asText();
            }
        } catch (Exception e) {
            logger.error("Error getting place name in language {} for placeId: {}", language, placeId, e);
        }
        return null;
    }
    
    private String extractCountryCode(JsonNode addressComponents) {
        for (JsonNode component : addressComponents) {
            JsonNode types = component.path("types");
            for (JsonNode type : types) {
                if ("country".equals(type.asText())) {
                    return component.path("short_name").asText();
                }
            }
        }
        return null;
    }
    
    private String extractCountryName(JsonNode addressComponents) {
        for (JsonNode component : addressComponents) {
            JsonNode types = component.path("types");
            for (JsonNode type : types) {
                if ("country".equals(type.asText())) {
                    return component.path("long_name").asText();
                }
            }
        }
        return null;
    }

    /**
     * Increment API call counters
     */
    private void incrementApiCallCounters() {
        try {
            if (statisticsService == null) {
                logger.error("StatisticsService is null!");
                return;
            }
            statisticsService.resetDailyCountersIfNeeded();
            statisticsService.incrementStat("total_google_places_api_calls");
            statisticsService.incrementStat("today_google_places_api_calls");
            statisticsService.incrementStat("week_google_places_api_calls");
            statisticsService.incrementStat("month_google_places_api_calls");
        } catch (Exception e) {
            logger.error("Error incrementing API call counters", e);
        }
    }
    
    /**
     * Get Google Places API statistics
     */
    public java.util.Map<String, Object> getApiStatistics() {
        return statisticsService.getGooglePlacesApiStatistics();
    }
    
    /**
     * Reset all counters (for testing purposes)
     */
    public void resetCounters() {
        statisticsService.resetAllCounters();
    }
}
