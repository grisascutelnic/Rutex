package com.scutelnic.rutex.util;

import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.entity.Ride;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class RideUrlBuilder {

    private static final Pattern TRAILING_ID_PATTERN = Pattern.compile("(?:^|.*-)(\\d+)$");

    public String buildRidePath(String language, RideDTO ride) {
        if (ride == null) {
            return "/" + normalizeLanguage(language) + "/rides";
        }
        return buildRidePath(language, ride.getId(), ride.getFromLocation(), ride.getToLocation());
    }

    public String buildRidePath(String language, Ride ride) {
        if (ride == null) {
            return "/" + normalizeLanguage(language) + "/rides";
        }
        return buildRidePath(language, ride.getId(), ride.getFromLocation(), ride.getToLocation());
    }

    public String buildRidePath(String language, Long rideId, String fromLocation, String toLocation) {
        return "/" + normalizeLanguage(language) + "/ride/" + buildRideSlug(rideId, fromLocation, toLocation);
    }

    public String buildRideSlug(Long rideId, String fromLocation, String toLocation) {
        if (rideId == null) {
            return "ride";
        }

        String routeSlug = slugify(cityName(fromLocation)) + "-" + slugify(cityName(toLocation));
        routeSlug = routeSlug.replaceAll("-+", "-").replaceAll("^-|-$", "");
        if (routeSlug.isBlank()) {
            routeSlug = "ride";
        }

        return routeSlug + "-" + rideId;
    }

    public Long extractRideId(String rideSlug) {
        if (rideSlug == null || rideSlug.isBlank()) {
            return null;
        }

        Matcher matcher = TRAILING_ID_PATTERN.matcher(rideSlug.trim());
        if (!matcher.matches()) {
            return null;
        }

        try {
            return Long.valueOf(matcher.group(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String normalizeLanguage(String language) {
        return "ru".equalsIgnoreCase(language) ? "ru" : "ro";
    }

    private String cityName(String location) {
        if (location == null) {
            return "";
        }
        int commaIndex = location.indexOf(',');
        return commaIndex >= 0 ? location.substring(0, commaIndex) : location;
    }

    private String slugify(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        return normalized
                .replaceAll("[^\\p{L}\\p{N}]+", "-")
                .replaceAll("^-|-$", "");
    }
}
