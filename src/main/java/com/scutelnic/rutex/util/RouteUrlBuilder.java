package com.scutelnic.rutex.util;

import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.entity.Ride;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.Locale;

@Component
public class RouteUrlBuilder {

    public String buildRoutePath(String language, RideDTO ride) {
        if (ride == null) {
            return "/" + normalizeLanguage(language) + "/rides";
        }
        return buildRoutePath(language, ride.getFromLocation(), ride.getToLocation());
    }

    public String buildRoutePath(String language, Ride ride) {
        if (ride == null) {
            return "/" + normalizeLanguage(language) + "/rides";
        }
        return buildRoutePath(language, ride.getFromLocation(), ride.getToLocation());
    }

    public String buildRoutePath(String language, String fromLocation, String toLocation) {
        return "/" + normalizeLanguage(language) + "/routes/" + buildRouteSlug(fromLocation, toLocation);
    }

    public String buildRouteSlug(String fromLocation, String toLocation) {
        String routeSlug = slugify(cityName(fromLocation)) + "-" + slugify(cityName(toLocation));
        routeSlug = routeSlug.replaceAll("-+", "-").replaceAll("^-|-$", "");
        return routeSlug.isBlank() ? "route" : routeSlug;
    }

    public String cityName(String location) {
        if (location == null) {
            return "";
        }
        int commaIndex = location.indexOf(',');
        return commaIndex >= 0 ? location.substring(0, commaIndex).trim() : location.trim();
    }

    private String normalizeLanguage(String language) {
        return "ru".equalsIgnoreCase(language) ? "ru" : "ro";
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
