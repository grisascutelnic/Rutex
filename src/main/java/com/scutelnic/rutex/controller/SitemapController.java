package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.util.RideUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class SitemapController {

    private static final DateTimeFormatter LASTMOD_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final RideRepository rideRepository;
    private final RideUrlBuilder rideUrlBuilder;
    private final String baseUrl;

    public SitemapController(RideRepository rideRepository,
                             RideUrlBuilder rideUrlBuilder,
                             @Value("${app.base-url:https://rutex.md}") String baseUrl) {
        this.rideRepository = rideRepository;
        this.rideUrlBuilder = rideUrlBuilder;
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public ResponseEntity<String> sitemap() {
        StringBuilder sitemap = new StringBuilder();
        sitemap.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sitemap.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        appendUrl(sitemap, "/", null, "daily", "1.0");
        appendUrl(sitemap, "/ro", null, "daily", "1.0");
        appendUrl(sitemap, "/ru", null, "daily", "0.9");
        appendUrl(sitemap, "/ro/rides", null, "hourly", "0.9");
        appendUrl(sitemap, "/ru/rides", null, "hourly", "0.8");
        appendUrl(sitemap, "/ro/about", null, "monthly", "0.5");
        appendUrl(sitemap, "/ru/about", null, "monthly", "0.4");
        appendUrl(sitemap, "/ro/contact", null, "monthly", "0.5");
        appendUrl(sitemap, "/ru/contact", null, "monthly", "0.4");
        appendUrl(sitemap, "/ro/terms", null, "monthly", "0.3");
        appendUrl(sitemap, "/ru/terms", null, "monthly", "0.3");
        appendUrl(sitemap, "/ro/privacy", null, "monthly", "0.3");
        appendUrl(sitemap, "/ru/privacy", null, "monthly", "0.3");

        List<Ride> activeRides = rideRepository.findAllActiveRides();
        for (Ride ride : activeRides) {
            String lastmod = formatLastmod(ride.getCreatedAt());
            appendUrl(sitemap, rideUrlBuilder.buildRidePath("ro", ride), lastmod, "daily", "0.8");
            appendUrl(sitemap, rideUrlBuilder.buildRidePath("ru", ride), lastmod, "daily", "0.7");
        }

        sitemap.append("</urlset>\n");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_XML)
                .body(sitemap.toString());
    }

    private void appendUrl(StringBuilder sitemap, String path, String lastmod, String changefreq, String priority) {
        sitemap.append("    <url>\n");
        sitemap.append("        <loc>").append(escapeXml(baseUrl + path)).append("</loc>\n");
        if (lastmod != null && !lastmod.isBlank()) {
            sitemap.append("        <lastmod>").append(lastmod).append("</lastmod>\n");
        }
        sitemap.append("        <changefreq>").append(changefreq).append("</changefreq>\n");
        sitemap.append("        <priority>").append(priority).append("</priority>\n");
        sitemap.append("    </url>\n");
    }

    private String formatLastmod(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return LASTMOD_FORMATTER.format(dateTime);
    }

    private String trimTrailingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "https://rutex.md";
        }
        String trimmed = value.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
