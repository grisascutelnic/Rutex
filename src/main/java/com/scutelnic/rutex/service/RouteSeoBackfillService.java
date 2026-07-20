package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.RouteSeoContent;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.repository.RouteSeoContentRepository;
import com.scutelnic.rutex.util.RouteUrlBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RouteSeoBackfillService {

    private final RideRepository rideRepository;
    private final RouteSeoContentRepository routeSeoContentRepository;
    private final RouteSeoContentService routeSeoContentService;
    private final RouteUrlBuilder routeUrlBuilder;

    public RouteSeoBackfillService(RideRepository rideRepository,
                                   RouteSeoContentRepository routeSeoContentRepository,
                                   RouteSeoContentService routeSeoContentService,
                                   RouteUrlBuilder routeUrlBuilder) {
        this.rideRepository = rideRepository;
        this.routeSeoContentRepository = routeSeoContentRepository;
        this.routeSeoContentService = routeSeoContentService;
        this.routeUrlBuilder = routeUrlBuilder;
    }

    @Async
    @EventListener(ApplicationReadyEvent.class)
    public void generateMissingRoutePages() {
        Map<String, RouteSeed> routes = new LinkedHashMap<>();
        for (RouteSeoContent page : routeSeoContentRepository.findAll()) {
            routes.putIfAbsent(pairKey(page.getFromLocation(), page.getToLocation()),
                    new RouteSeed(page.getRouteSlug(), page.getFromLocation(), page.getToLocation()));
        }
        for (Ride ride : rideRepository.findAll()) {
            String routeSlug = routeUrlBuilder.buildRouteSlug(ride.getFromLocation(), ride.getToLocation());
            if (routeSeoContentService.isRouteHidden(routeSlug)) {
                continue;
            }
            routes.putIfAbsent(pairKey(ride.getFromLocation(), ride.getToLocation()),
                    new RouteSeed(
                            routeSlug,
                            ride.getFromLocation(),
                            ride.getToLocation()
                    ));
        }

        for (RouteSeed route : routes.values()) {
            for (String language : List.of("ro", "ru")) {
                routeSeoContentService.getOrCreate(
                        route.routeSlug(),
                        language,
                        route.fromLocation(),
                        route.toLocation()
                );
            }
        }
    }

    private String pairKey(String fromLocation, String toLocation) {
        String from = normalize(routeUrlBuilder.cityName(fromLocation));
        String to = normalize(routeUrlBuilder.cityName(toLocation));
        return from.compareTo(to) <= 0 ? from + "|" + to : to + "|" + from;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private record RouteSeed(String routeSlug, String fromLocation, String toLocation) {}
}
