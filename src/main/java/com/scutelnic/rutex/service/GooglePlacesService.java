package com.scutelnic.rutex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scutelnic.rutex.entity.District;
import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.repository.DistrictRepository;
import com.scutelnic.rutex.repository.LocalityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

@Service
public class GooglePlacesService {
    
    private static final Logger logger = LoggerFactory.getLogger(GooglePlacesService.class);
    
    @Value("${google.places.api.key}")
    private String apiKey;
    
    @Value("${google.places.api.base-url:https://places.googleapis.com/v1}")
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
        if (query == null || query.isBlank() || limit <= 0) {
            return results;
        }
        
        try {
            incrementApiCallCounters();
            String normalizedLanguage = normalizeLanguage(language);
            String url = trimTrailingSlash(baseUrl) + "/places:autocomplete";
            Map<String, Object> requestBody = Map.of(
                    "input", query,
                    "includedPrimaryTypes", List.of("(cities)"),
                    "languageCode", normalizedLanguage
            );
            HttpHeaders headers = createHeaders("suggestions.placePrediction.placeId");

            logger.debug("Calling Google Places API autocomplete for language {}", normalizedLanguage);

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    new HttpEntity<>(requestBody, headers),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());

            for (JsonNode suggestion : root.path("suggestions")) {
                if (results.size() >= limit) {
                    break;
                }

                String placeId = suggestion.path("placePrediction").path("placeId").asText();
                if (placeId.isBlank()) {
                    continue;
                }

                Locality locality = getPlaceDetails(placeId, normalizedLanguage);
                if (locality != null) {
                    results.add(locality);
                }
            }
        } catch (Exception e) {
            logApiError("autocomplete", e);
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
                try {
                    return localityRepository.save(locality);
                } catch (DataIntegrityViolationException ex) {
                    logger.warn("Duplicate/constraint while updating locality for placeId {}. Returning existing record.", placeId);
                    return localityRepository.findByGooglePlaceId(placeId).orElse(locality);
                }
            }
            
            incrementApiCallCounters();
            String normalizedLanguage = normalizeLanguage(language);
            String url = UriComponentsBuilder
                    .fromUriString(trimTrailingSlash(baseUrl))
                    .pathSegment("places", placeId)
                    .queryParam("languageCode", normalizedLanguage)
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders("displayName,location,types,addressComponents")),
                    String.class
            );
            JsonNode result = objectMapper.readTree(response.getBody());
            String name = result.path("displayName").path("text").asText();
            JsonNode location = result.path("location");
            if (name.isBlank() || !location.has("latitude") || !location.has("longitude")) {
                logger.warn("Google Places API returned incomplete details for placeId {}", placeId);
                return null;
            }

            double lat = location.path("latitude").asDouble();
            double lng = location.path("longitude").asDouble();
            Locality.LocalityType type = determineLocalityType(result.path("types"));
            JsonNode addressComponents = result.path("addressComponents");
            District district = extractDistrict(addressComponents);
            String countryCode = extractCountryCode(addressComponents);
            String countryName = extractCountryName(addressComponents);

            Locality locality = new Locality();
            locality.setGooglePlaceId(placeId);
            locality.setLatitude(lat);
            locality.setLongitude(lng);
            locality.setType(type);
            locality.setDistrict(district);
            locality.setSearchCount(1);
            locality.setCountryCode(countryCode);
            locality.setCountryNameRo(countryName);
            locality.setCountryNameRu(countryName);

            if ("ru".equals(normalizedLanguage)) {
                locality.setNameRu(name);
                String romanianName = getRomanianName(placeId);
                locality.setNameRo(romanianName != null ? romanianName : name);
            } else {
                locality.setNameRo(name);
                String russianName = getRussianName(placeId);
                locality.setNameRu(russianName != null ? russianName : name);
            }

            try {
                return localityRepository.save(locality);
            } catch (DataIntegrityViolationException ex) {
                logger.warn("Duplicate locality insert for placeId {}. Returning existing record.", placeId);
                return localityRepository.findByGooglePlaceId(placeId).orElse(null);
            }
        } catch (DataIntegrityViolationException e) {
            logger.warn("Constraint violation for placeId {}. Returning existing locality if present.", placeId);
            return localityRepository.findByGooglePlaceId(placeId).orElse(null);
        } catch (Exception e) {
            logApiError("place details", e);
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
                    String districtName = componentText(component, "longText", "long_name");
                    if (districtName.isBlank()) {
                        continue;
                    }
                    
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
            incrementApiCallCounters();
            String url = UriComponentsBuilder
                    .fromUriString(trimTrailingSlash(baseUrl))
                    .pathSegment("places", placeId)
                    .queryParam("languageCode", normalizeLanguage(language))
                    .build()
                    .encode()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    new HttpEntity<>(createHeaders("displayName")),
                    String.class
            );
            JsonNode root = objectMapper.readTree(response.getBody());
            String name = root.path("displayName").path("text").asText();
            return name.isBlank() ? null : name;
        } catch (Exception e) {
            logApiError("localized place name", e);
        }
        return null;
    }
    
    private String extractCountryCode(JsonNode addressComponents) {
        for (JsonNode component : addressComponents) {
            JsonNode types = component.path("types");
            for (JsonNode type : types) {
                if ("country".equals(type.asText())) {
                    return componentText(component, "shortText", "short_name");
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
                    return componentText(component, "longText", "long_name");
                }
            }
        }
        return null;
    }

    private HttpHeaders createHeaders(String fieldMask) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Goog-Api-Key", apiKey);
        headers.set("X-Goog-FieldMask", fieldMask);
        return headers;
    }

    private String normalizeLanguage(String language) {
        return language != null && language.toLowerCase(Locale.ROOT).startsWith("ru") ? "ru" : "ro";
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://places.googleapis.com/v1";
        }
        return value.endsWith("/") ? value.substring(0, value.length() - 1) : value;
    }

    private String componentText(JsonNode component, String currentField, String legacyField) {
        String value = component.path(currentField).asText();
        return value.isBlank() ? component.path(legacyField).asText() : value;
    }

    private void logApiError(String operation, Exception exception) {
        if (exception instanceof RestClientResponseException responseException) {
            logger.error(
                    "Google Places API {} failed with status {}: {}",
                    operation,
                    responseException.getStatusCode().value(),
                    responseException.getResponseBodyAsString()
            );
            return;
        }
        logger.error("Google Places API {} failed: {}", operation, exception.getClass().getSimpleName());
        logger.debug("Google Places API {} failure details", operation, exception);
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
