package com.scutelnic.rutex.service;

import com.scutelnic.rutex.event.IndexNowUrlsChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.client.RestClient;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

@Service
public class IndexNowService {

    private static final Logger log = LoggerFactory.getLogger(IndexNowService.class);

    private final RestClient restClient;
    private final String baseUrl;
    private final String key;
    private final boolean enabled;

    public IndexNowService(RestClient.Builder restClientBuilder,
                           @Value("${app.base-url:https://rutex.md}") String baseUrl,
                           @Value("${indexnow.key:}") String key,
                           @Value("${indexnow.enabled:false}") boolean enabled,
                           @Value("${indexnow.endpoint:https://api.indexnow.org/indexnow}") String endpoint) {
        this.restClient = restClientBuilder.baseUrl(endpoint).build();
        this.baseUrl = trimTrailingSlash(baseUrl);
        this.key = key == null ? "" : key.trim();
        this.enabled = enabled;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void submitChangedUrls(IndexNowUrlsChangedEvent event) {
        if (!enabled || key.isBlank() || event == null || event.paths().isEmpty()) {
            return;
        }

        List<String> urls = event.paths().stream()
                .filter(path -> path != null && !path.isBlank())
                .map(this::absoluteUrl)
                .collect(java.util.stream.Collectors.collectingAndThen(
                        java.util.stream.Collectors.toCollection(LinkedHashSet::new),
                        List::copyOf
                ));
        if (urls.isEmpty()) {
            return;
        }

        try {
            restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of(
                            "host", hostName(),
                            "key", key,
                            "keyLocation", baseUrl + "/" + key + ".txt",
                            "urlList", urls
                    ))
                    .retrieve()
                    .toBodilessEntity();
            log.info("IndexNow accepted {} changed URL(s)", urls.size());
        } catch (Exception exception) {
            log.warn("IndexNow submission failed for {} URL(s): {}", urls.size(), exception.getMessage());
        }
    }

    public boolean isValidKey(String candidate) {
        return enabled && !key.isBlank() && key.equals(candidate);
    }

    public String key() {
        return key;
    }

    private String absoluteUrl(String path) {
        if (path.startsWith("https://") || path.startsWith("http://")) {
            return path;
        }
        return baseUrl + (path.startsWith("/") ? path : "/" + path);
    }

    private String hostName() {
        return java.net.URI.create(baseUrl).getHost();
    }

    private String trimTrailingSlash(String value) {
        String normalized = value == null || value.isBlank() ? "https://rutex.md" : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }
}
