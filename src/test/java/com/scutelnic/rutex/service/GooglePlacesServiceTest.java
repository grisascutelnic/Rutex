package com.scutelnic.rutex.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scutelnic.rutex.entity.District;
import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.repository.DistrictRepository;
import com.scutelnic.rutex.repository.LocalityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GooglePlacesServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private LocalityRepository localityRepository;

    @Mock
    private DistrictRepository districtRepository;

    @Mock
    private StatisticsService statisticsService;

    private GooglePlacesService googlePlacesService;

    @BeforeEach
    void setUp() {
        googlePlacesService = new GooglePlacesService(
                restTemplate,
                new ObjectMapper(),
                localityRepository,
                districtRepository,
                statisticsService
        );
        ReflectionTestUtils.setField(googlePlacesService, "apiKey", "test-secret-key");
        ReflectionTestUtils.setField(googlePlacesService, "baseUrl", "https://places.googleapis.com/v1");
    }

    @Test
    void searchesWithPlacesApiNewWithoutPuttingKeyInUrl() {
        String autocompleteResponse = """
                {
                  "suggestions": [
                    {
                      "placePrediction": {
                        "placeId": "place-cahul"
                      }
                    }
                  ]
                }
                """;
        String detailsResponse = """
                {
                  "displayName": {"text": "Cahul"},
                  "location": {"latitude": 45.9042, "longitude": 28.1993},
                  "types": ["locality", "political"],
                  "addressComponents": [
                    {
                      "longText": "Raionul Cahul",
                      "shortText": "Cahul",
                      "types": ["administrative_area_level_1"]
                    },
                    {
                      "longText": "Republica Moldova",
                      "shortText": "MD",
                      "types": ["country"]
                    }
                  ]
                }
                """;
        String russianNameResponse = """
                {
                  "displayName": {"text": "Кагул"}
                }
                """;

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(autocompleteResponse));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(ResponseEntity.ok(detailsResponse), ResponseEntity.ok(russianNameResponse));
        when(localityRepository.findByGooglePlaceId("place-cahul")).thenReturn(Optional.empty());
        when(districtRepository.findByNameContainingIgnoreCase("Raionul Cahul")).thenReturn(List.of());
        when(districtRepository.save(any(District.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(localityRepository.save(any(Locality.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<Locality> results = googlePlacesService.searchLocalities("Cah", "ro", 5);

        assertEquals(1, results.size());
        Locality locality = results.getFirst();
        assertEquals("place-cahul", locality.getGooglePlaceId());
        assertEquals("Cahul", locality.getNameRo());
        assertEquals("Кагул", locality.getNameRu());
        assertEquals("MD", locality.getCountryCode());
        assertEquals("Republica Moldova", locality.getCountryNameRo());
        assertEquals(Locality.LocalityType.CITY, locality.getType());
        assertEquals("Raionul Cahul", locality.getDistrict().getNameRo());

        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<HttpMethod> methodCaptor = ArgumentCaptor.forClass(HttpMethod.class);
        ArgumentCaptor<HttpEntity> entityCaptor = ArgumentCaptor.forClass(HttpEntity.class);
        verify(restTemplate, times(3)).exchange(
                urlCaptor.capture(),
                methodCaptor.capture(),
                entityCaptor.capture(),
                eq(String.class)
        );

        assertEquals("https://places.googleapis.com/v1/places:autocomplete", urlCaptor.getAllValues().getFirst());
        assertEquals(HttpMethod.POST, methodCaptor.getAllValues().getFirst());
        for (String url : urlCaptor.getAllValues()) {
            assertFalse(url.contains("test-secret-key"));
        }
        for (HttpEntity entity : entityCaptor.getAllValues()) {
            assertEquals("test-secret-key", entity.getHeaders().getFirst("X-Goog-Api-Key"));
        }

        Object requestBody = entityCaptor.getAllValues().getFirst().getBody();
        assertEquals("(cities)", ((List<?>) ((Map<?, ?>) requestBody).get("includedPrimaryTypes")).getFirst());
        assertEquals("ro", ((Map<?, ?>) requestBody).get("languageCode"));
    }
}
