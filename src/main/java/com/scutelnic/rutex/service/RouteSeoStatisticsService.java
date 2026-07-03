package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.RouteSeoContent;
import com.scutelnic.rutex.entity.RouteSeoPageEvent;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.repository.RouteSeoContentRepository;
import com.scutelnic.rutex.repository.RouteSeoPageEventRepository;
import com.scutelnic.rutex.util.RouteUrlBuilder;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class RouteSeoStatisticsService {

    private static final Set<String> ALLOWED_EVENT_TYPES = Set.of("view", "ride_click", "add_ride_click");

    private final RouteSeoContentRepository routeSeoContentRepository;
    private final RouteSeoPageEventRepository routeSeoPageEventRepository;
    private final RideRepository rideRepository;
    private final RouteUrlBuilder routeUrlBuilder;
    private final String baseUrl;

    public RouteSeoStatisticsService(RouteSeoContentRepository routeSeoContentRepository,
                                     RouteSeoPageEventRepository routeSeoPageEventRepository,
                                     RideRepository rideRepository,
                                     RouteUrlBuilder routeUrlBuilder,
                                     @Value("${app.base-url:https://rutex.md}") String baseUrl) {
        this.routeSeoContentRepository = routeSeoContentRepository;
        this.routeSeoPageEventRepository = routeSeoPageEventRepository;
        this.rideRepository = rideRepository;
        this.routeUrlBuilder = routeUrlBuilder;
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @Transactional
    public void recordEvent(String routeSlug,
                            String language,
                            String eventType,
                            String visitorKey,
                            Long userId,
                            Long rideId,
                            String pageUrl,
                            String referrer,
                            HttpServletRequest request) {
        if (isBlank(routeSlug) || isBlank(language) || isBlank(eventType) || isBlank(visitorKey)) {
            return;
        }
        if (!ALLOWED_EVENT_TYPES.contains(eventType)) {
            return;
        }

        String userAgent = request.getHeader("User-Agent");
        if (isBotUserAgent(userAgent)) {
            return;
        }

        RouteSeoPageEvent event = new RouteSeoPageEvent();
        event.setRouteSlug(limit(routeSlug, 180));
        event.setLanguage(limit(language, 2));
        event.setEventType(eventType);
        event.setVisitorKey(limit(visitorKey, 80));
        event.setUserId(userId);
        event.setRideId(rideId);
        event.setPageUrl(limit(pageUrl, 500));
        event.setReferrer(limit(referrer, 500));
        event.setUserAgent(limit(userAgent, 500));
        routeSeoPageEventRepository.save(event);
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getAdminStatistics() {
        List<RouteSeoContent> pages = routeSeoContentRepository.findAll();
        List<Ride> activeRides = rideRepository.findAllActiveRides();

        Map<String, Integer> activeRideCounts = new HashMap<>();
        Set<String> activeRouteKeys = new HashSet<>();
        for (Ride ride : activeRides) {
            String slug = routeUrlBuilder.buildRouteSlug(ride.getFromLocation(), ride.getToLocation());
            for (String language : List.of("ro", "ru")) {
                String key = key(slug, language);
                activeRouteKeys.add(key);
                activeRideCounts.merge(key, 1, Integer::sum);
            }
        }

        Map<String, EventStats> allStats = buildEventStats(routeSeoPageEventRepository.aggregateAll());
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayStart = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        Map<String, EventStats> todayStats = buildEventStats(routeSeoPageEventRepository.aggregateSince(todayStart));
        Map<String, EventStats> last7Stats = buildEventStats(routeSeoPageEventRepository.aggregateSince(now.minusDays(7)));
        Map<String, EventStats> last30Stats = buildEventStats(routeSeoPageEventRepository.aggregateSince(now.minusDays(30)));
        Map<String, String> topReferrers = buildTopReferrers();

        List<Map<String, Object>> rows = new ArrayList<>();
        long totalViews = 0;
        long totalUniqueUsers = 0;
        long totalViews30 = 0;
        long totalUniqueUsers30 = 0;
        long totalClicks30 = 0;
        int activeInSitemap = 0;

        for (RouteSeoContent page : pages) {
            String routeKey = key(page.getRouteSlug(), page.getLanguage());
            EventStats total = allStats.getOrDefault(routeKey, new EventStats());
            EventStats today = todayStats.getOrDefault(routeKey, new EventStats());
            EventStats last7 = last7Stats.getOrDefault(routeKey, new EventStats());
            EventStats last30 = last30Stats.getOrDefault(routeKey, new EventStats());
            int activeRidesForPage = activeRideCounts.getOrDefault(routeKey, 0);
            boolean inSitemap = activeRouteKeys.contains(routeKey);
            if (inSitemap) {
                activeInSitemap++;
            }

            totalViews30 += last30.views;
            totalUniqueUsers30 += last30.uniqueUsers;
            totalClicks30 += last30.clicks;
            totalViews += total.views;
            totalUniqueUsers += total.uniqueUsers;

            Map<String, Object> row = new HashMap<>();
            String routeTitle = routeUrlBuilder.cityName(page.getFromLocation()) + " -> " + routeUrlBuilder.cityName(page.getToLocation());
            row.put("routeSlug", page.getRouteSlug());
            row.put("language", page.getLanguage());
            row.put("routeTitle", routeTitle);
            row.put("fromLocation", page.getFromLocation());
            row.put("toLocation", page.getToLocation());
            row.put("url", baseUrl + routeUrlBuilder.buildRoutePath(page.getLanguage(), page.getFromLocation(), page.getToLocation()));
            row.put("createdAt", page.getCreatedAt());
            row.put("status", inSitemap ? "În sitemap" : "Fără cursă activă");
            row.put("activeInSitemap", inSitemap);
            row.put("activeRides", activeRidesForPage);
            row.put("views", total.views);
            row.put("viewsToday", today.views);
            row.put("views7Days", last7.views);
            row.put("views30Days", last30.views);
            row.put("uniqueUsers", total.uniqueUsers);
            row.put("uniqueUsersToday", today.uniqueUsers);
            row.put("uniqueUsers7Days", last7.uniqueUsers);
            row.put("uniqueUsers30Days", last30.uniqueUsers);
            row.put("lastVisit", total.lastVisit);
            row.put("clicks", total.clicks);
            row.put("clicks30Days", last30.clicks);
            row.put("ctr", total.views > 0 ? Math.round((total.clicks * 10000.0 / total.views)) / 100.0 : 0);
            row.put("topReferrer", topReferrers.getOrDefault(routeKey, "Direct"));
            rows.add(row);
        }

        rows.sort(Comparator
                .comparing((Map<String, Object> row) -> (LocalDateTime) row.get("lastVisit"), Comparator.nullsLast(Comparator.reverseOrder()))
                .thenComparing(row -> (String) row.get("routeTitle")));

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalPages", pages.size());
        summary.put("activeInSitemap", activeInSitemap);
        summary.put("totalViews", totalViews);
        summary.put("totalUniqueUsers", totalUniqueUsers);
        summary.put("views30Days", totalViews30);
        summary.put("uniqueUsers30Days", totalUniqueUsers30);
        summary.put("clicks30Days", totalClicks30);

        Map<String, Object> response = new HashMap<>();
        response.put("summary", summary);
        response.put("pages", rows);
        return response;
    }

    private Map<String, EventStats> buildEventStats(List<RouteSeoPageEventRepository.RouteSeoEventAggregate> aggregates) {
        Map<String, EventStats> stats = new HashMap<>();
        for (RouteSeoPageEventRepository.RouteSeoEventAggregate aggregate : aggregates) {
            EventStats eventStats = stats.computeIfAbsent(key(aggregate.getRouteSlug(), aggregate.getLanguage()), ignored -> new EventStats());
            long totalEvents = aggregate.getTotalEvents() != null ? aggregate.getTotalEvents() : 0;
            long uniqueVisitors = aggregate.getUniqueVisitors() != null ? aggregate.getUniqueVisitors() : 0;
            if ("view".equals(aggregate.getEventType())) {
                eventStats.views += totalEvents;
                eventStats.uniqueUsers = Math.max(eventStats.uniqueUsers, uniqueVisitors);
                eventStats.lastVisit = latest(eventStats.lastVisit, aggregate.getLastEventAt());
            } else {
                eventStats.clicks += totalEvents;
            }
        }
        return stats;
    }

    private Map<String, String> buildTopReferrers() {
        Map<String, ReferrerCount> best = new HashMap<>();
        for (RouteSeoPageEventRepository.RouteSeoReferrerAggregate aggregate : routeSeoPageEventRepository.aggregateReferrers()) {
            String routeKey = key(aggregate.getRouteSlug(), aggregate.getLanguage());
            String referrer = normalizeReferrer(aggregate.getReferrer());
            long count = aggregate.getTotalEvents() != null ? aggregate.getTotalEvents() : 0;
            ReferrerCount existing = best.get(routeKey);
            if (existing == null || count > existing.count) {
                best.put(routeKey, new ReferrerCount(referrer, count));
            }
        }

        Map<String, String> topReferrers = new HashMap<>();
        best.forEach((routeKey, referrerCount) -> topReferrers.put(routeKey, referrerCount.referrer));
        return topReferrers;
    }

    private String normalizeReferrer(String referrer) {
        if (referrer == null || referrer.isBlank()) {
            return "Direct";
        }
        String lower = referrer.toLowerCase(Locale.ROOT);
        if (lower.contains("google.")) {
            return "Google";
        }
        if (lower.contains("yandex.")) {
            return "Yandex";
        }
        if (lower.contains("bing.")) {
            return "Bing";
        }
        if (lower.contains("facebook.") || lower.contains("m.facebook.")) {
            return "Facebook";
        }
        return referrer;
    }

    private boolean isBotUserAgent(String userAgent) {
        if (userAgent == null || userAgent.isBlank()) {
            return true;
        }
        String lower = userAgent.toLowerCase(Locale.ROOT);
        return lower.contains("bot")
                || lower.contains("crawl")
                || lower.contains("spider")
                || lower.contains("slurp")
                || lower.contains("ahrefs")
                || lower.contains("semrush")
                || lower.contains("mj12")
                || lower.contains("dotbot")
                || lower.contains("python-requests")
                || lower.contains("curl")
                || lower.contains("wget");
    }

    private LocalDateTime latest(LocalDateTime current, LocalDateTime candidate) {
        if (candidate == null) {
            return current;
        }
        if (current == null || candidate.isAfter(current)) {
            return candidate;
        }
        return current;
    }

    private String key(String routeSlug, String language) {
        return (routeSlug == null ? "" : routeSlug) + "|" + (language == null ? "" : language);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private String limit(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
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

    private static class EventStats {
        private long views;
        private long uniqueUsers;
        private long clicks;
        private LocalDateTime lastVisit;
    }

    private static class ReferrerCount {
        private final String referrer;
        private final long count;

        private ReferrerCount(String referrer, long count) {
            this.referrer = referrer;
            this.count = count;
        }
    }
}
