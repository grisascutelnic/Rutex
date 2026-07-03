package com.scutelnic.rutex.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scutelnic.rutex.entity.RouteSeoContent;
import com.scutelnic.rutex.repository.RouteSeoContentRepository;
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
    private final ObjectMapper objectMapper;
    private final RouteUrlBuilder routeUrlBuilder;
    private final RestTemplate openAiRestTemplate;
    private final String apiKey;
    private final String apiBaseUrl;
    private final String model;

    public RouteSeoContentService(RouteSeoContentRepository routeSeoContentRepository,
                                  ObjectMapper objectMapper,
                                  RouteUrlBuilder routeUrlBuilder,
                                  @Value("${openai.api-key:}") String apiKey,
                                  @Value("${openai.api.base-url:https://api.openai.com/v1}") String apiBaseUrl,
                                  @Value("${openai.route-seo.model:gpt-4.1-mini}") String model) {
        this.routeSeoContentRepository = routeSeoContentRepository;
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
            return existing;
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
                Generate route page content in %s for:
                From: %s
                To: %s

                Content requirements:
                - routeDescription: 2-3 short sentences about transport on this route and checking active rides on Rutex.
                - fromDescription: 2 short sentences about the departure locality, useful for transport context.
                - toDescription: 2 short sentences about the destination locality, useful for transport context.
                - nearbyDirectionsText: write 2-3 sentences.
                  Start with wording equivalent to "Posibile localitati sau segmente pe aceeasi directie:".
                  Mention 2-3 possible full-route variants from departure to destination using intermediate localities, for example "Chisinau - Orhei - Floresti - Soroca" or another plausible variant such as one via a larger nearby city when geographically plausible.
                  Also include 4-7 partial route combinations in the form "Locality - Locality", including step-by-step segments and wider combinations such as "Chisinau - Floresti" or "Orhei - Soroca" when plausible.
                  The text must mention actual locality names, not generic phrases like "localitati din apropiere".
                  Explain that these are possible/related searches and depend on the route chosen by the driver.
                - frequentSearchesText: 6-8 frequent search phrases for this route, each phrase on a separate line.
                  Use natural phrases like "transport Chisinau - Soroca", "masina Chisinau - Soroca", "cursa Chisinau - Soroca", "locuri disponibile Chisinau - Soroca", "transport colete Chisinau - Soroca".
                  Keep it useful and not spammy.
                  Do not add links. Do not say the route definitely passes through those localities.
                - No FAQ.
                - No markdown.
                - Keep every field under 900 characters.
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
