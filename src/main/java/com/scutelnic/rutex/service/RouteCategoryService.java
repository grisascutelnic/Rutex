package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.RouteCategoryDTO;
import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.RouteSeoContent;
import com.scutelnic.rutex.repository.LocalityRepository;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.repository.RouteSeoContentRepository;
import com.scutelnic.rutex.repository.RouteSeoPageEventRepository;
import com.scutelnic.rutex.util.RouteUrlBuilder;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class RouteCategoryService {

    public enum CategoryType {
        MOLDOVA,
        INTERNATIONAL
    }

    private static final Set<String> MOLDOVA_COUNTRY_NAMES = Set.of(
            "moldova", "republica moldova", "republic of moldova", "молдова", "республика молдова"
    );

    private static final Map<String, Integer> CITY_PRIORITY = Map.ofEntries(
            Map.entry("chisinau", 1_000_000),
            Map.entry("кишинев", 1_000_000),
            Map.entry("кишинэу", 1_000_000),
            Map.entry("balti", 900_000),
            Map.entry("бельцы", 900_000),
            Map.entry("cahul", 800_000),
            Map.entry("кагул", 800_000),
            Map.entry("orhei", 700_000),
            Map.entry("оргеев", 700_000),
            Map.entry("soroca", 600_000),
            Map.entry("сороки", 600_000),
            Map.entry("ungheni", 500_000),
            Map.entry("унгены", 500_000)
    );

    private final RouteSeoContentRepository routeSeoContentRepository;
    private final RouteSeoPageEventRepository routeSeoPageEventRepository;
    private final LocalityRepository localityRepository;
    private final RideRepository rideRepository;
    private final RouteUrlBuilder routeUrlBuilder;

    public RouteCategoryService(RouteSeoContentRepository routeSeoContentRepository,
                                RouteSeoPageEventRepository routeSeoPageEventRepository,
                                LocalityRepository localityRepository,
                                RideRepository rideRepository,
                                RouteUrlBuilder routeUrlBuilder) {
        this.routeSeoContentRepository = routeSeoContentRepository;
        this.routeSeoPageEventRepository = routeSeoPageEventRepository;
        this.localityRepository = localityRepository;
        this.rideRepository = rideRepository;
        this.routeUrlBuilder = routeUrlBuilder;
    }

    public List<RouteCategoryDTO> getCategories(String language, CategoryType categoryType) {
        List<RouteSeoContent> pages = allKnownRoutePages(language);
        LocalityIndex localityIndex = buildLocalityIndex();
        Map<String, Integer> cityFrequency = buildCityFrequency(pages);
        Map<String, Long> routeViews = buildRouteViews();
        Map<String, RoutePair> pairs = new LinkedHashMap<>();

        for (RouteSeoContent page : pages) {
            if (page.isHidden()) {
                continue;
            }
            if (categoryFor(page, localityIndex) != categoryType) {
                continue;
            }

            String fromCity = routeUrlBuilder.cityName(page.getFromLocation());
            String toCity = routeUrlBuilder.cityName(page.getToLocation());
            String displayFrom = displayName(page.getDisplayFromName(), fromCity);
            String displayTo = displayName(page.getDisplayToName(), toCity);
            String fromKey = normalize(fromCity);
            String toKey = normalize(toCity);
            if (fromKey.isBlank() || toKey.isBlank() || fromKey.equals(toKey)) {
                continue;
            }

            String pairKey = fromKey.compareTo(toKey) <= 0
                    ? fromKey + "|" + toKey
                    : toKey + "|" + fromKey;
            pairs.computeIfAbsent(pairKey, ignored -> createPair(
                    language, page, displayFrom, displayTo, fromKey, toKey, localityIndex, cityFrequency, routeViews
            ));
        }

        List<RoutePair> sortedPairs = new ArrayList<>(pairs.values());
        sortedPairs.sort(Comparator
                .comparingLong(RoutePair::viewCount).reversed()
                .thenComparing(Comparator.comparingInt(RoutePair::relevance).reversed())
                .thenComparing(RoutePair::primaryCity, String.CASE_INSENSITIVE_ORDER)
                .thenComparing(RoutePair::secondaryCity, String.CASE_INSENSITIVE_ORDER));

        return sortedPairs.stream()
                .map(pair -> new RouteCategoryDTO(pair.primaryCity(), pair.secondaryCity(), pair.routePath(), pair.viewCount()))
                .toList();
    }

    private List<RouteSeoContent> allKnownRoutePages(String language) {
        List<RouteSeoContent> pages = new ArrayList<>(
                routeSeoContentRepository.findByLanguageOrderByUpdatedAtDesc(language)
        );
        Set<String> knownSlugs = pages.stream()
                .map(RouteSeoContent::getRouteSlug)
                .collect(java.util.stream.Collectors.toSet());
        Set<String> hiddenSlugs = pages.stream()
                .filter(RouteSeoContent::isHidden)
                .map(RouteSeoContent::getRouteSlug)
                .collect(java.util.stream.Collectors.toSet());

        for (Ride ride : rideRepository.findAll()) {
            String routeSlug = routeUrlBuilder.buildRouteSlug(ride.getFromLocation(), ride.getToLocation());
            if (hiddenSlugs.contains(routeSlug) || !knownSlugs.add(routeSlug)) {
                continue;
            }
            RouteSeoContent routePage = new RouteSeoContent();
            routePage.setRouteSlug(routeSlug);
            routePage.setLanguage(language);
            routePage.setFromLocation(ride.getFromLocation());
            routePage.setToLocation(ride.getToLocation());
            pages.add(routePage);
        }
        return pages;
    }

    private RoutePair createPair(String language,
                                 RouteSeoContent page,
                                 String fromCity,
                                 String toCity,
                                 String fromKey,
                                 String toKey,
                                 LocalityIndex localityIndex,
                                 Map<String, Integer> cityFrequency,
                                 Map<String, Long> routeViews) {
        int fromRelevance = relevance(fromKey, localityIndex, cityFrequency);
        int toRelevance = relevance(toKey, localityIndex, cityFrequency);
        boolean fromFirst = fromRelevance > toRelevance
                || (fromRelevance == toRelevance && fromCity.compareToIgnoreCase(toCity) <= 0);
        String primaryCity = fromFirst ? fromCity : toCity;
        String secondaryCity = fromFirst ? toCity : fromCity;
        String routePath = "/" + language + "/routes/" + page.getRouteSlug();
        return new RoutePair(primaryCity, secondaryCity, routePath,
                Math.max(fromRelevance, toRelevance), routeViews.getOrDefault(page.getRouteSlug(), 0L));
    }

    private Map<String, Long> buildRouteViews() {
        Map<String, Long> views = new HashMap<>();
        for (RouteSeoPageEventRepository.RouteViewAggregate aggregate : routeSeoPageEventRepository.aggregateViewsByRouteSlug()) {
            views.put(aggregate.getRouteSlug(), aggregate.getTotalViews() == null ? 0L : aggregate.getTotalViews());
        }
        return views;
    }

    private CategoryType categoryFor(RouteSeoContent page, LocalityIndex localityIndex) {
        CountryStatus fromCountry = countryStatus(page.getFromLocation(), localityIndex);
        CountryStatus toCountry = countryStatus(page.getToLocation(), localityIndex);
        return fromCountry == CountryStatus.FOREIGN || toCountry == CountryStatus.FOREIGN
                ? CategoryType.INTERNATIONAL
                : CategoryType.MOLDOVA;
    }

    private CountryStatus countryStatus(String location, LocalityIndex localityIndex) {
        if (location == null || location.isBlank()) {
            return CountryStatus.UNKNOWN;
        }
        String[] parts = location.split(",");
        String country = normalize(parts[parts.length - 1]);
        if (MOLDOVA_COUNTRY_NAMES.contains(country)) {
            return CountryStatus.MOLDOVA;
        }
        if (parts.length > 1 && looksLikeCountry(parts[parts.length - 1])) {
            return CountryStatus.FOREIGN;
        }
        return localityIndex.moldovaCities().contains(normalize(routeUrlBuilder.cityName(location)))
                ? CountryStatus.MOLDOVA
                : CountryStatus.UNKNOWN;
    }

    private boolean looksLikeCountry(String value) {
        String normalized = normalize(value);
        return normalized.matches("romania|rumania|germany|germania|deutschland|france|franta|italy|italia|"
                + "spain|spania|portugal|poland|polonia|ukraine|ucraina|turkey|turcia|"
                + "united kingdom|regatul unit|ireland|irlanda|netherlands|olanda|belgium|belgia|"
                + "austria|switzerland|elvetia|czechia|cehia|russia|rusia|"
                + "румыния|германия|франция|италия|испания|португалия|польша|украина|турция|"
                + "великобритания|ирландия|нидерланды|бельгия|австрия|швейцария|чехия|россия");
    }

    private Map<String, Integer> buildCityFrequency(List<RouteSeoContent> pages) {
        Map<String, Integer> frequency = new HashMap<>();
        for (RouteSeoContent page : pages) {
            frequency.merge(normalize(routeUrlBuilder.cityName(page.getFromLocation())), 1, Integer::sum);
            frequency.merge(normalize(routeUrlBuilder.cityName(page.getToLocation())), 1, Integer::sum);
        }
        return frequency;
    }

    private LocalityIndex buildLocalityIndex() {
        Set<String> moldovaCities = new java.util.HashSet<>();
        Map<String, Integer> popularity = new HashMap<>();
        for (Locality locality : localityRepository.findAll()) {
            boolean isMoldova = "MD".equalsIgnoreCase(locality.getCountryCode());
            int searchCount = locality.getSearchCount() == null ? 0 : locality.getSearchCount();
            for (String name : new String[]{locality.getNameRo(), locality.getNameRu()}) {
                String key = normalize(name);
                if (key.isBlank()) {
                    continue;
                }
                if (isMoldova) {
                    moldovaCities.add(key);
                }
                popularity.merge(key, searchCount, Math::max);
            }
        }
        return new LocalityIndex(moldovaCities, popularity);
    }

    private int relevance(String cityKey, LocalityIndex localityIndex, Map<String, Integer> cityFrequency) {
        return CITY_PRIORITY.getOrDefault(cityKey, 0)
                + localityIndex.popularity().getOrDefault(cityKey, 0) * 100
                + cityFrequency.getOrDefault(cityKey, 0);
    }

    private String displayName(String customName, String fallback) {
        return customName == null || customName.isBlank() ? fallback : customName.trim();
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

    private record LocalityIndex(Set<String> moldovaCities, Map<String, Integer> popularity) {}

    private record RoutePair(String primaryCity, String secondaryCity, String routePath, int relevance, long viewCount) {}

    private enum CountryStatus {
        MOLDOVA,
        FOREIGN,
        UNKNOWN
    }
}
