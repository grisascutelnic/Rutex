package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.RouteSeoContent;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.service.PageModelService;
import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.service.RouteSeoContentService;
import com.scutelnic.rutex.util.RouteUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RoutePageController {

    private final PageModelService pageModelService;
    private final RideRepository rideRepository;
    private final RideService rideService;
    private final RouteSeoContentService routeSeoContentService;
    private final RouteUrlBuilder routeUrlBuilder;
    private final String baseUrl;

    public RoutePageController(PageModelService pageModelService,
                               RideRepository rideRepository,
                               RideService rideService,
                               RouteSeoContentService routeSeoContentService,
                               RouteUrlBuilder routeUrlBuilder,
                               @Value("${app.base-url:https://rutex.md}") String baseUrl) {
        this.pageModelService = pageModelService;
        this.rideRepository = rideRepository;
        this.rideService = rideService;
        this.routeSeoContentService = routeSeoContentService;
        this.routeUrlBuilder = routeUrlBuilder;
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @GetMapping("/ro/routes/{routeSlug}")
    public String routeRo(@PathVariable String routeSlug, Model model, HttpSession session) {
        return buildRoutePage("ro", routeSlug, model, session);
    }

    @GetMapping("/ru/routes/{routeSlug}")
    public String routeRu(@PathVariable String routeSlug, Model model, HttpSession session) {
        return buildRoutePage("ru", routeSlug, model, session);
    }

    private String buildRoutePage(String language, String routeSlug, Model model, HttpSession session) {
        rideService.markCompletedRidesAsInactive();

        Optional<Ride> routeSource = findRouteSource(routeSlug);
        if (routeSource.isEmpty()) {
            return "redirect:/" + language + "/rides";
        }

        Ride source = routeSource.get();
        String canonicalPath = routeUrlBuilder.buildRoutePath(language, source);
        if (!canonicalPath.endsWith("/" + routeSlug)) {
            return "redirect:" + canonicalPath;
        }

        String fromLocation = source.getFromLocation();
        String toLocation = source.getToLocation();
        String fromCity = routeUrlBuilder.cityName(fromLocation);
        String toCity = routeUrlBuilder.cityName(toLocation);

        List<RideDTO> routeRides = rideService.getAllActiveRides().stream()
                .filter(ride -> sameRoute(ride, fromLocation, toLocation))
                .collect(Collectors.toList());

        pageModelService.addCurrentUserToModel(model, session);
        pageModelService.addTranslationsToModel(model, "rides", language);
        pageModelService.setLanguageInModel(model, language);

        String routeTitle = fromCity + " - " + toCity;
        model.addAttribute("routeTitle", routeTitle);
        model.addAttribute("routeFrom", fromCity);
        model.addAttribute("routeTo", toCity);
        model.addAttribute("routeFromFull", fromLocation);
        model.addAttribute("routeToFull", toLocation);
        model.addAttribute("routeSlug", routeSlug);
        model.addAttribute("routeRides", routeRides);
        model.addAttribute("allRides", routeRides);
        model.addAttribute("canonicalRouteUrl", baseUrl + canonicalPath);
        model.addAttribute("routeSeoTitle", buildSeoTitle(language, routeTitle));
        model.addAttribute("routeSeoDescription", buildSeoDescription(language, routeTitle, routeRides.size()));
        Optional<RouteSeoContent> seoContent = routeSeoContentService.getOrCreate(
                routeSlug,
                language,
                fromLocation,
                toLocation
        );
        model.addAttribute("routeSeoContent", seoContent.orElse(null));

        return "route-details";
    }

    private Optional<Ride> findRouteSource(String routeSlug) {
        return rideRepository.findAll().stream()
                .filter(ride -> Boolean.TRUE.equals(ride.getIsActive()))
                .filter(ride -> routeUrlBuilder.buildRouteSlug(ride.getFromLocation(), ride.getToLocation()).equals(routeSlug))
                .findFirst();
    }

    private boolean sameRoute(RideDTO ride, String fromLocation, String toLocation) {
        return normalize(ride.getFromLocation()).equals(normalize(fromLocation))
                && normalize(ride.getToLocation()).equals(normalize(toLocation));
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }

    private String buildSeoTitle(String language, String routeTitle) {
        if ("ru".equals(language)) {
            return routeTitle + " | Поездки на Rutex";
        }
        return "Curse " + routeTitle + " | Rutex";
    }

    private String buildSeoDescription(String language, String routeTitle, int rideCount) {
        if ("ru".equals(language)) {
            return "Найдите актуальные поездки по маршруту " + routeTitle + " на Rutex. Доступно поездок: " + rideCount + ".";
        }
        return "Gaseste curse active pe ruta " + routeTitle + " pe Rutex. Curse disponibile: " + rideCount + ".";
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
}
