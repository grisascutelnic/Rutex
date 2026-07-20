package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.RouteSeoContent;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.repository.RouteSeoContentRepository;
import com.scutelnic.rutex.service.PageModelService;
import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.service.RouteSeoContentService;
import com.scutelnic.rutex.util.RouteUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpSession;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class RoutePageController {

    private final PageModelService pageModelService;
    private final RideRepository rideRepository;
    private final RideService rideService;
    private final RouteSeoContentService routeSeoContentService;
    private final RouteSeoContentRepository routeSeoContentRepository;
    private final RouteUrlBuilder routeUrlBuilder;
    private final String baseUrl;

    public RoutePageController(PageModelService pageModelService,
                               RideRepository rideRepository,
                               RideService rideService,
                               RouteSeoContentService routeSeoContentService,
                               RouteSeoContentRepository routeSeoContentRepository,
                               RouteUrlBuilder routeUrlBuilder,
                               @Value("${app.base-url:https://rutex.md}") String baseUrl) {
        this.pageModelService = pageModelService;
        this.rideRepository = rideRepository;
        this.rideService = rideService;
        this.routeSeoContentService = routeSeoContentService;
        this.routeSeoContentRepository = routeSeoContentRepository;
        this.routeUrlBuilder = routeUrlBuilder;
        this.baseUrl = trimTrailingSlash(baseUrl);
    }

    @GetMapping("/ro/routes/{routeSlug}")
    public Object routeRo(@PathVariable String routeSlug, Model model, HttpSession session) {
        return buildRoutePage("ro", routeSlug, model, session);
    }

    @GetMapping("/ru/routes/{routeSlug}")
    public Object routeRu(@PathVariable String routeSlug, Model model, HttpSession session) {
        return buildRoutePage("ru", routeSlug, model, session);
    }

    private Object buildRoutePage(String language, String routeSlug, Model model, HttpSession session) {
        rideService.markCompletedRidesAsInactive();

        Optional<String> redirectSlug = routeSeoContentService.findRedirect(routeSlug);
        if (redirectSlug.isPresent()) {
            RedirectView redirectView = new RedirectView("/" + language + "/routes/" + redirectSlug.get(), true);
            redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);
            return redirectView;
        }

        if (routeSeoContentService.isRouteHidden(routeSlug)) {
            return "redirect:/" + language + "/rides";
        }

        Optional<RouteLocations> routeLocations = findRouteLocations(language, routeSlug);
        if (routeLocations.isEmpty()) {
            return "redirect:/" + language + "/rides";
        }

        String canonicalPath = "/" + language + "/routes/" + routeSlug;
        String fromLocation = routeLocations.get().fromLocation();
        String toLocation = routeLocations.get().toLocation();
        String fromCity = routeUrlBuilder.cityName(fromLocation);
        String toCity = routeUrlBuilder.cityName(toLocation);

        List<RideDTO> routeRides = rideService.getAllActiveRides().stream()
                .filter(ride -> sameRoute(ride, fromLocation, toLocation))
                .collect(Collectors.toList());
        long directRideCount = routeRides.stream()
                .filter(ride -> isDirectRoute(ride, fromLocation, toLocation))
                .count();
        long reverseRideCount = routeRides.size() - directRideCount;

        pageModelService.addCurrentUserToModel(model, session);
        pageModelService.addTranslationsToModel(model, "rides", language);
        pageModelService.setLanguageInModel(model, language);

        Optional<RouteSeoContent> seoContent = routeSeoContentService.getOrCreate(
                routeSlug,
                language,
                fromLocation,
                toLocation
        );
        String displayFrom = seoContent.map(RouteSeoContent::getDisplayFromName)
                .filter(value -> !value.isBlank())
                .orElse(fromCity);
        String displayTo = seoContent.map(RouteSeoContent::getDisplayToName)
                .filter(value -> !value.isBlank())
                .orElse(toCity);
        String routeTitle = displayFrom + " – " + displayTo;
        model.addAttribute("routeTitle", routeTitle);
        model.addAttribute("routeFrom", displayFrom);
        model.addAttribute("routeTo", displayTo);
        model.addAttribute("routeFromFull", fromLocation);
        model.addAttribute("routeToFull", toLocation);
        model.addAttribute("routeSlug", routeSlug);
        model.addAttribute("routeRides", routeRides);
        model.addAttribute("allRides", routeRides);
        model.addAttribute("directRideCount", directRideCount);
        model.addAttribute("reverseRideCount", reverseRideCount);
        model.addAttribute("directRouteSearches", buildFrequentSearches(language, displayFrom, displayTo));
        model.addAttribute("reverseRouteSearches", buildFrequentSearches(language, displayTo, displayFrom));
        model.addAttribute("canonicalRouteUrl", baseUrl + canonicalPath);
        model.addAttribute("routeSeoTitle", buildSeoTitle(language, routeTitle));
        model.addAttribute("routeSeoDescription", buildSeoDescription(language, routeTitle, routeRides.size()));
        model.addAttribute("routeSeoContent", seoContent.orElse(null));

        return "route-details";
    }

    private Optional<RouteLocations> findRouteLocations(String language, String routeSlug) {
        Optional<RouteSeoContent> storedPage = routeSeoContentRepository.findByRouteSlugAndLanguage(routeSlug, language);
        if (storedPage.isPresent()) {
            RouteSeoContent page = storedPage.get();
            return Optional.of(new RouteLocations(page.getFromLocation(), page.getToLocation()));
        }

        return rideRepository.findAll().stream()
                .filter(ride -> routeUrlBuilder.buildRouteSlug(ride.getFromLocation(), ride.getToLocation()).equals(routeSlug))
                .map(ride -> new RouteLocations(ride.getFromLocation(), ride.getToLocation()))
                .findFirst();
    }

    private boolean sameRoute(RideDTO ride, String fromLocation, String toLocation) {
        boolean direct = isDirectRoute(ride, fromLocation, toLocation);
        boolean reverse = normalize(ride.getFromLocation()).equals(normalize(toLocation))
                && normalize(ride.getToLocation()).equals(normalize(fromLocation));
        return direct || reverse;
    }

    private boolean isDirectRoute(RideDTO ride, String fromLocation, String toLocation) {
        return normalize(ride.getFromLocation()).equals(normalize(fromLocation))
                && normalize(ride.getToLocation()).equals(normalize(toLocation));
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String cityName = routeUrlBuilder.cityName(value);
        return Normalizer.normalize(cityName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String buildSeoTitle(String language, String routeTitle) {
        if ("ru".equals(language)) {
            return routeTitle + " | Поездки на Rutex";
        }
        return "Curse " + routeTitle + " | Rutex";
    }

    private String buildSeoDescription(String language, String routeTitle, int rideCount) {
        if ("ru".equals(language)) {
            return "Найдите актуальные поездки в обоих направлениях по маршруту " + routeTitle + " на Rutex. Доступно поездок: " + rideCount + ".";
        }
        return "Găsește curse active în ambele direcții pe ruta " + routeTitle + " pe Rutex. Curse disponibile: " + rideCount + ".";
    }

    private List<String> buildFrequentSearches(String language, String fromCity, String toCity) {
        String route = fromCity + " - " + toCity;
        if ("ru".equals(language)) {
            return List.of(
                    "транспорт " + route,
                    "поездка " + route,
                    "машина " + route,
                    "свободные места " + route,
                    "перевозка посылок " + route,
                    "микроавтобус " + route
            );
        }
        return List.of(
                "transport " + route,
                "cursă " + route,
                "mașină " + route,
                "locuri disponibile " + route,
                "transport colete " + route,
                "microbuz " + route
        );
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

    private record RouteLocations(String fromLocation, String toLocation) {}
}
