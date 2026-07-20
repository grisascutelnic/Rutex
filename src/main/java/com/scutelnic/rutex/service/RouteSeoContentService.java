package com.scutelnic.rutex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scutelnic.rutex.dto.RouteSeoContentUpdateRequest;
import com.scutelnic.rutex.dto.RouteMoveRequest;
import com.scutelnic.rutex.entity.RouteSeoRedirect;
import com.scutelnic.rutex.entity.RouteSeoContent;
import com.scutelnic.rutex.repository.RouteSeoContentRepository;
import com.scutelnic.rutex.repository.RouteSeoPageEventRepository;
import com.scutelnic.rutex.repository.RouteSeoRedirectRepository;
import com.scutelnic.rutex.util.RouteUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class RouteSeoContentService {

    private final RouteSeoContentRepository routeSeoContentRepository;
    private final RouteSeoRedirectRepository routeSeoRedirectRepository;
    private final RouteSeoPageEventRepository routeSeoPageEventRepository;
    private final ObjectMapper objectMapper;
    private final RouteUrlBuilder routeUrlBuilder;
    private final RestTemplate openAiRestTemplate;
    private final String apiKey;
    private final String apiBaseUrl;
    private final String model;

    public RouteSeoContentService(RouteSeoContentRepository routeSeoContentRepository,
                                  RouteSeoRedirectRepository routeSeoRedirectRepository,
                                  RouteSeoPageEventRepository routeSeoPageEventRepository,
                                  ObjectMapper objectMapper,
                                  RouteUrlBuilder routeUrlBuilder,
                                  @Value("${openai.api-key:}") String apiKey,
                                  @Value("${openai.api.base-url:https://api.openai.com/v1}") String apiBaseUrl,
                                  @Value("${openai.route-seo.model:gpt-4.1-mini}") String model) {
        this.routeSeoContentRepository = routeSeoContentRepository;
        this.routeSeoRedirectRepository = routeSeoRedirectRepository;
        this.routeSeoPageEventRepository = routeSeoPageEventRepository;
        this.objectMapper = objectMapper;
        this.routeUrlBuilder = routeUrlBuilder;
        this.apiKey = apiKey;
        this.apiBaseUrl = trimTrailingSlash(apiBaseUrl);
        this.model = model;

        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(8000);
        factory.setReadTimeout(30000);
        this.openAiRestTemplate = new RestTemplate(factory);
    }

    @Async
    public void preGenerateForRoute(String fromLocation, String toLocation) {
        if (fromLocation == null || fromLocation.isBlank() || toLocation == null || toLocation.isBlank()) {
            return;
        }
        String routeSlug = routeUrlBuilder.buildRouteSlug(fromLocation, toLocation);
        for (String language : List.of("ro", "ru")) {
            try {
                getOrCreate(routeSlug, language, fromLocation, toLocation);
            } catch (Exception e) {
                System.err.println("Route SEO pre-generation failed for " + routeSlug + " (" + language + "): " + e.getMessage());
            }
        }
    }

    @Transactional
    public Optional<RouteSeoContent> getOrCreate(String routeSlug,
                                                 String language,
                                                 String fromLocation,
                                                 String toLocation) {
        Optional<RouteSeoContent> existing = routeSeoContentRepository.findByRouteSlugAndLanguage(routeSlug, language);
        if (existing.isPresent()) {
            return existing.filter(content -> !content.isHidden());
        }
        if (isRouteHidden(routeSlug)) {
            return Optional.empty();
        }

        if (apiKey == null || apiKey.isBlank()) {
            return Optional.empty();
        }

        try {
            RouteSeoContent generated = generate(routeSlug, language, fromLocation, toLocation);
            return Optional.of(routeSeoContentRepository.save(generated));
        } catch (Exception e) {
            System.err.println("Route SEO generation failed for " + routeSlug + " (" + language + "): " + e.getMessage());
            return Optional.empty();
        }
    }

    public RouteSeoContent regenerate(String routeSlug, String language) {
        RouteSeoContent existing = routeSeoContentRepository.findByRouteSlugAndLanguage(routeSlug, language)
                .orElseThrow(() -> new IllegalArgumentException("Pagina SEO a rutei nu a fost găsită."));
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("Cheia API OpenAI nu este configurată.");
        }

        try {
            RouteSeoContent generated = generate(
                    routeSlug,
                    language,
                    existing.getFromLocation(),
                    existing.getToLocation()
            );
            existing.setRouteDescription(generated.getRouteDescription());
            existing.setFromDescription(generated.getFromDescription());
            existing.setToDescription(generated.getToDescription());
            existing.setNearbyDirectionsText(generated.getNearbyDirectionsText());
            existing.setFrequentSearchesText(generated.getFrequentSearchesText());
            existing.setSource(generated.getSource());
            return routeSeoContentRepository.save(existing);
        } catch (Exception e) {
            throw new IllegalStateException("Regenerarea conținutului AI a eșuat: " + e.getMessage(), e);
        }
    }

    @Transactional
    public RouteSeoContent updateManually(String routeSlug,
                                          String language,
                                          RouteSeoContentUpdateRequest request) {
        RouteSeoContent content = routeSeoContentRepository.findByRouteSlugAndLanguage(routeSlug, language)
                .orElseThrow(() -> new IllegalArgumentException("Pagina SEO a rutei nu a fost găsită."));

        content.setRouteDescription(requiredText(request.routeDescription(), "Descrierea rutei", 2000));
        content.setFromDescription(requiredText(request.fromDescription(), "Descrierea localității de plecare", 2000));
        content.setToDescription(requiredText(request.toDescription(), "Descrierea localității de destinație", 2000));
        content.setNearbyDirectionsText(requiredText(request.nearbyDirectionsText(), "Direcțiile apropiate", 2000));
        content.setFrequentSearchesText(requiredText(request.frequentSearchesText(), "Căutările frecvente", 2000));
        content.setSource("admin:manual");
        content.setAdminVerified(true);
        return routeSeoContentRepository.save(content);
    }

    @Transactional
    public String moveRoute(String oldSlug, RouteMoveRequest request) {
        String fromLocation = requiredText(request.fromLocation(), "Localitatea de plecare", 255);
        String toLocation = requiredText(request.toLocation(), "Localitatea de destinație", 255);
        String newSlug = routeUrlBuilder.buildRouteSlug(fromLocation, toLocation);
        List<RouteSeoContent> pages = routeSeoContentRepository.findAllByRouteSlug(oldSlug);
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("Pagina SEO a rutei nu a fost găsită.");
        }

        if (newSlug.equals(oldSlug)) {
            for (RouteSeoContent page : pages) {
                page.setFromLocation(fromLocation);
                page.setToLocation(toLocation);
                page.setDisplayFromName(routeUrlBuilder.cityName(fromLocation));
                page.setDisplayToName(routeUrlBuilder.cityName(toLocation));
            }
            routeSeoContentRepository.saveAll(pages);
            return oldSlug;
        }
        if (!routeSeoContentRepository.findAllByRouteSlug(newSlug).isEmpty()) {
            throw new IllegalArgumentException("Există deja o pagină pentru noul traseu.");
        }

        for (RouteSeoContent page : pages) {
            page.setRouteSlug(newSlug);
            page.setFromLocation(fromLocation);
            page.setToLocation(toLocation);
            page.setDisplayFromName(routeUrlBuilder.cityName(fromLocation));
            page.setDisplayToName(routeUrlBuilder.cityName(toLocation));
            page.setHidden(false);
        }
        routeSeoContentRepository.saveAll(pages);
        routeSeoPageEventRepository.moveEventsToSlug(oldSlug, newSlug);

        RouteSeoRedirect redirect = routeSeoRedirectRepository.findByOldSlug(oldSlug).orElseGet(RouteSeoRedirect::new);
        redirect.setOldSlug(oldSlug);
        redirect.setNewSlug(newSlug);
        routeSeoRedirectRepository.save(redirect);
        return newSlug;
    }

    public Optional<String> findRedirect(String routeSlug) {
        return routeSeoRedirectRepository.findByOldSlug(routeSlug).map(RouteSeoRedirect::getNewSlug);
    }

    @Transactional
    public void hideRoute(String routeSlug) {
        List<RouteSeoContent> pages = routeSeoContentRepository.findAllByRouteSlug(routeSlug);
        if (pages.isEmpty()) {
            throw new IllegalArgumentException("Pagina SEO a rutei nu a fost găsită.");
        }
        pages.forEach(page -> page.setHidden(true));
        routeSeoContentRepository.saveAll(pages);
    }

    public boolean isRouteHidden(String routeSlug) {
        return routeSeoContentRepository.existsByRouteSlugAndHiddenTrue(routeSlug);
    }

    private String requiredText(String value, String fieldName, int maxLength) {
        String trimmed = value == null ? "" : value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(fieldName + " nu poate fi goală.");
        }
        if (trimmed.length() > maxLength) {
            throw new IllegalArgumentException(fieldName + " depășește limita de " + maxLength + " caractere.");
        }
        return trimmed;
    }

    private RouteSeoContent generate(String routeSlug, String language, String fromLocation, String toLocation)
            throws Exception {
        String responseText = callOpenAi(language, fromLocation, toLocation);
        JsonNode json = objectMapper.readTree(responseText);

        RouteSeoContent content = new RouteSeoContent();
        content.setRouteSlug(routeSlug);
        content.setLanguage(language);
        content.setFromLocation(fromLocation);
        content.setToLocation(toLocation);
        content.setRouteDescription(text(json, "routeDescription"));
        content.setFromDescription(text(json, "fromDescription"));
        content.setToDescription(text(json, "toDescription"));
        content.setNearbyDirectionsText(text(json, "nearbyDirectionsText"));
        content.setFrequentSearchesText(text(json, "frequentSearchesText"));
        content.setSource("openai:" + model);
        return content;
    }

    private String callOpenAi(String language, String fromLocation, String toLocation) throws Exception {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey.trim());

        Map<String, Object> request = Map.of(
                "model", model,
                "input", List.of(
                        Map.of(
                                "role", "developer",
                                "content", "You generate concise SEO content for public route pages on a ride-sharing website. Return only valid JSON matching the schema. Do not include FAQ. Do not include links. The nearbyDirectionsText field must be concrete, not generic: include 2-3 possible full-route variants from origin to destination using intermediate localities, plus partial segments in the format 'City - City'. The frequentSearchesText field must contain 6-8 short natural search phrases, not a spammy keyword list. Do not claim certainty about intermediate towns; use cautious wording such as 'posibile', 'poate include', 'pot fi relevante', or 'in functie de traseul ales de sofer'."
                        ),
                        Map.of(
                                "role", "user",
                                "content", buildPrompt(language, fromLocation, toLocation)
                        )
                ),
                "text", Map.of(
                        "format", Map.of(
                                "type", "json_schema",
                                "name", "route_seo_content",
                                "strict", true,
                                "schema", Map.of(
                                        "type", "object",
                                        "additionalProperties", false,
                                        "properties", Map.of(
                                                "routeDescription", Map.of("type", "string"),
                                                "fromDescription", Map.of("type", "string"),
                                                "toDescription", Map.of("type", "string"),
                                                "nearbyDirectionsText", Map.of("type", "string"),
                                                "frequentSearchesText", Map.of("type", "string")
                                        ),
                                        "required", List.of("routeDescription", "fromDescription", "toDescription", "nearbyDirectionsText", "frequentSearchesText")
                                )
                        )
                ),
                "max_output_tokens", 1200
        );

        try {
            String response = openAiRestTemplate.postForObject(
                    apiBaseUrl + "/responses",
                    new HttpEntity<>(request, headers),
                    String.class
            );
            String outputText = extractOutputText(response);
            if (outputText == null || outputText.isBlank()) {
                throw new IllegalStateException("OpenAI response did not contain output text.");
            }
            return outputText;
        } catch (RestClientException e) {
            throw new IllegalStateException("OpenAI request failed: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(String language, String fromLocation, String toLocation) {
        String targetLanguage = "ru".equals(language) ? "Russian" : "Romanian";
        return """
                Generate bidirectional route page content in %s for the locality pair:
                First locality: %s
                Second locality: %s

                Content requirements:
                - Treat the route as bidirectional and naturally mention travel in both directions.
                - Use simple, direct and user-friendly language. Avoid filler, repetition and long sentences.
                - routeDescription: 2 short sentences about transport between the two localities in both directions and checking active rides on Rutex.
                - fromDescription: 1-2 short sentences about the first locality, useful for transport context. Do not describe it only as a departure point.
                - toDescription: 1-2 short sentences about the second locality, useful for transport context. Do not describe it only as a destination.
                - nearbyDirectionsText: write 1-2 short sentences.
                  Start with wording equivalent to "Posibile localitati sau segmente pe aceeasi directie:".
                  Mention 2-3 possible full-route variants from departure to destination using intermediate localities, for example "Chisinau - Orhei - Floresti - Soroca" or another plausible variant such as one via a larger nearby city when geographically plausible.
                  Also include 3-4 useful partial route combinations in the form "Locality - Locality".
                  The text must mention actual locality names, not generic phrases like "localitati din apropiere".
                  Explain that these are possible/related searches in either direction and depend on the route chosen by the driver.
                - frequentSearchesText: 4-6 frequent search phrases for this route, each phrase on a separate line.
                  Include natural phrases for both directions, such as "transport Chisinau - Soroca" and "transport Soroca - Chisinau".
                  Keep it useful and not spammy.
                  Do not add links. Do not say the route definitely passes through those localities.
                - No FAQ.
                - No markdown.
                - Keep every field under 550 characters.
                """.formatted(targetLanguage, fromLocation, toLocation);
    }

    private String extractOutputText(String response) throws Exception {
        JsonNode root = objectMapper.readTree(response);
        JsonNode output = root.path("output");
        if (!output.isArray()) {
            return null;
        }

        for (JsonNode item : output) {
            JsonNode content = item.path("content");
            if (!content.isArray()) {
                continue;
            }
            for (JsonNode contentItem : content) {
                JsonNode text = contentItem.path("text");
                if (text.isTextual()) {
                    return text.asText();
                }
            }
        }
        return null;
    }

    private String text(JsonNode json, String field) {
        JsonNode value = json.path(field);
        return value.isTextual() ? value.asText().trim() : "";
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://api.openai.com/v1";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }
}
