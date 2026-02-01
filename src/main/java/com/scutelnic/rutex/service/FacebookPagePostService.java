package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.RideDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;

@Service
public class FacebookPagePostService {

    private final RestTemplate restTemplate;

    @Value("${facebook.page-id:}")
    private String pageId;

    @Value("${facebook.page-access-token:}")
    private String pageAccessToken;

    @Value("${facebook.api.base-url:https://graph.facebook.com}")
    private String apiBaseUrl;

    @Value("${facebook.api.version:}")
    private String apiVersion;

    @Value("${app.base-url:}")
    private String baseUrlConfig;

    public FacebookPagePostService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String postRideToPage(RideDTO ride, String language, HttpServletRequest request) {
        validateConfig();

        String normalizedLanguage = "ru".equalsIgnoreCase(language) ? "ru" : "ro";
        String baseUrl = resolveBaseUrl(request);
        String rideLink = baseUrl + "/" + normalizedLanguage + "/ride/" + ride.getId();
        String message = buildMessage(ride, normalizedLanguage, rideLink);

        MultiValueMap<String, String> payload = new LinkedMultiValueMap<>();
        payload.add("message", message);
        payload.add("link", rideLink);
        payload.add("access_token", pageAccessToken.trim());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(payload, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(buildApiUrl(), requestEntity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Publicarea pe Facebook a eșuat. Cod: " + response.getStatusCode());
        }

        Map body = response.getBody();
        if (body != null && body.get("id") != null) {
            return body.get("id").toString();
        }

        return "";
    }

    private void validateConfig() {
        if (pageId == null || pageId.trim().isEmpty()) {
            throw new IllegalStateException("facebook.page-id nu este configurat.");
        }
        if (pageAccessToken == null || pageAccessToken.trim().isEmpty()) {
            throw new IllegalStateException("facebook.page-access-token nu este configurat.");
        }
    }

    private String buildApiUrl() {
        String base = apiBaseUrl != null ? apiBaseUrl.trim() : "https://graph.facebook.com";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        String version = apiVersion != null ? apiVersion.trim() : "";
        if (!version.isEmpty()) {
            if (version.startsWith("/")) {
                version = version.substring(1);
            }
            return base + "/" + version + "/" + pageId + "/feed";
        }

        return base + "/" + pageId + "/feed";
    }

    private String buildMessage(RideDTO ride, String language, String rideLink) {
        String from = safe(ride.getFromLocation());
        String to = safe(ride.getToLocation());

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ro", "RO"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", new Locale("ro", "RO"));

        String travelDate = ride.getTravelDate() != null ? ride.getTravelDate().format(dateFormatter) : "-";
        String departureTime = ride.getDepartureTime() != null ? ride.getDepartureTime().format(timeFormatter) : "-";

        boolean isPackageOnly = Boolean.TRUE.equals(ride.getIsPackageOnly());
        boolean transportAndPackages = Boolean.TRUE.equals(ride.getTransportAndPackages());

        if ("ru".equals(language)) {
            String seatsText = isPackageOnly ? "Только посылки" : ("Мест: " + safeNumber(ride.getAvailableSeats()));
            if (!isPackageOnly && transportAndPackages) {
                seatsText += " (пассажиры + посылки)";
            }
            return String.format("Новая поездка: %s -> %s%nДата: %s | Время: %s%n%s%nДетали: %s",
                from, to, travelDate, departureTime, seatsText, rideLink);
        }

        String seatsText = isPackageOnly ? "Transport doar colete" : ("Locuri: " + safeNumber(ride.getAvailableSeats()));
        if (!isPackageOnly && transportAndPackages) {
            seatsText += " (pasageri + colete)";
        }
        return String.format("Cursă nouă: %s -> %s%nData: %s | Ora: %s%n%s%nDetalii: %s",
            from, to, travelDate, departureTime, seatsText, rideLink);
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        if (baseUrlConfig != null && !baseUrlConfig.trim().isEmpty()) {
            return trimTrailingSlash(baseUrlConfig.trim());
        }
        return trimTrailingSlash(buildBaseUrl(request));
    }

    private String buildBaseUrl(HttpServletRequest request) {
        String proto = optionalHeader(request, "X-Forwarded-Proto");
        if (proto.isBlank()) {
            proto = request.getScheme();
        }

        String host = optionalHeader(request, "X-Forwarded-Host");
        if (host.isBlank()) {
            host = request.getServerName();
        }

        String portHeader = optionalHeader(request, "X-Forwarded-Port");
        String portPart = "";

        if (!host.contains(":")) {
            if (!portHeader.isBlank()) {
                if (!("80".equals(portHeader) || "443".equals(portHeader))) {
                    portPart = ":" + portHeader;
                }
            } else {
                int port = request.getServerPort();
                if (("http".equalsIgnoreCase(proto) && port != 80)
                    || ("https".equalsIgnoreCase(proto) && port != 443)) {
                    portPart = ":" + port;
                }
            }
        }

        return proto + "://" + host + portPart;
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String optionalHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeNumber(Integer value) {
        return value == null ? "-" : value.toString();
    }
}
