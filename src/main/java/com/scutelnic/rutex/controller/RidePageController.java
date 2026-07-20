package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.RouteCategoryDTO;
import com.scutelnic.rutex.service.PageModelService;
import com.scutelnic.rutex.service.RouteCategoryService;
import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.util.RideUrlBuilder;
import com.scutelnic.rutex.util.RouteUrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;

@Controller
public class RidePageController {

	@Autowired
	private PageModelService pageModelService;

	@Autowired
	private RideService rideService;

	@Autowired
	private RideUrlBuilder rideUrlBuilder;

	@Autowired
	private RouteUrlBuilder routeUrlBuilder;

	@Autowired
	private RouteCategoryService routeCategoryService;

	@Value("${app.base-url:https://rutex.md}")
	private String baseUrl;

	@GetMapping("/ro/rides")
	public String ridesRo(@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) String date,
			@RequestParam(required = false) String packages,
			@RequestParam(defaultValue = "0") int page,
			Model model, HttpSession session) {
		return buildCategoryPage(model, session, "ro", from, to, date, packages, page);
	}

	@GetMapping("/ru/rides")
	public String ridesRu(@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) String date,
			@RequestParam(required = false) String packages,
			@RequestParam(defaultValue = "0") int page,
			Model model, HttpSession session) {
		return buildCategoryPage(model, session, "ru", from, to, date, packages, page);
	}

	@GetMapping("/ro/ride/{id:[0-9]+}")
	public RedirectView legacyRideDetailsRo(@PathVariable Long id) {
		return redirectToCanonicalRide("ro", id);
	}

	@GetMapping("/ru/ride/{id:[0-9]+}")
	public RedirectView legacyRideDetailsRu(@PathVariable Long id) {
		return redirectToCanonicalRide("ru", id);
	}

	@GetMapping("/ro/ride/{rideSlug}")
	public String rideDetailsRo(@PathVariable String rideSlug, Model model, HttpSession session, HttpServletRequest request) {
		return pageModelService.buildRideDetailsPage(model, session, request, rideSlug);
	}

	@GetMapping("/ru/ride/{rideSlug}")
	public String rideDetailsRu(@PathVariable String rideSlug, Model model, HttpSession session, HttpServletRequest request) {
		return pageModelService.buildRideDetailsPage(model, session, request, rideSlug);
	}

	private RedirectView redirectToCanonicalRide(String language, Long rideId) {
		RedirectView redirectView;
		try {
			redirectView = new RedirectView(rideUrlBuilder.buildRidePath(language, rideService.getRideById(rideId)));
		} catch (Exception e) {
			redirectView = new RedirectView("/" + language + "/rides");
		}
		redirectView.setStatusCode(org.springframework.http.HttpStatus.MOVED_PERMANENTLY);
		return redirectView;
	}

	private String buildCategoryPage(Model model,
			HttpSession session,
			String language,
			String from,
			String to,
			String date,
			String packages,
			int page) {
		boolean searchActive = hasSearchFilters(from, to, date, packages);
		Boolean packagesBoolean = "on".equals(packages) || "true".equals(packages) ? true : null;
		if (searchActive) {
			pageModelService.buildRidesPageModel(model, session, language, from, to, date, packagesBoolean, page, 9);
		} else {
			pageModelService.addCurrentUserToModel(model, session);
			pageModelService.addTranslationsToModel(model, "rides", language);
			pageModelService.setLanguageInModel(model, language);
			model.addAttribute("filterFrom", "");
			model.addAttribute("filterTo", "");
			model.addAttribute("filterDate", "");
			model.addAttribute("filterPackages", false);
		}

		String path = "/" + language + "/rides";
		model.addAttribute("searchActive", searchActive);
		model.addAttribute("categoryPagePath", path);
		List<RouteCategoryDTO> allCategories = routeCategoryService.getCategories(language, RouteCategoryService.CategoryType.ALL);
		int categoryPageSize = 12;
		int categoryTotalPages = Math.max(1, (int) Math.ceil((double) allCategories.size() / categoryPageSize));
		int categoryCurrentPage = Math.max(0, Math.min(page, categoryTotalPages - 1));
		int categoryStart = Math.min(categoryCurrentPage * categoryPageSize, allCategories.size());
		int categoryEnd = Math.min(categoryStart + categoryPageSize, allCategories.size());
		model.addAttribute("routeCategories", allCategories.subList(categoryStart, categoryEnd));
		model.addAttribute("categoryCurrentPage", categoryCurrentPage);
		model.addAttribute("categoryTotalPages", categoryTotalPages);
		model.addAttribute("categoryTotalRoutes", allCategories.size());
		model.addAttribute("categoryHasPreviousPage", categoryCurrentPage > 0);
		model.addAttribute("categoryHasNextPage", categoryCurrentPage < categoryTotalPages - 1);
		model.addAttribute("categoryTitle", "ru".equals(language) ? "Поездки" : "Călătorii");
		model.addAttribute("categoryDescription", "ru".equals(language)
				? "Найдите поездки по Молдове и международным направлениям на Rutex."
				: "Găsește curse în Moldova și pe trasee internaționale, într-un singur catalog Rutex.");
		model.addAttribute("canonicalCategoryUrl", trimTrailingSlash(baseUrl) + path);
		return "ride-categories";
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

	private boolean hasSearchFilters(String from, String to, String date, String packages) {
		return (from != null && !from.isBlank())
				|| (to != null && !to.isBlank())
				|| (date != null && !date.isBlank())
				|| "on".equals(packages)
				|| "true".equals(packages);
	}
}


