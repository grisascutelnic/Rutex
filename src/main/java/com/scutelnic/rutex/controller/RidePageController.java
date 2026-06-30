package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.PageModelService;
import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.util.RideUrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class RidePageController {

	@Autowired
	private PageModelService pageModelService;

	@Autowired
	private RideService rideService;

	@Autowired
	private RideUrlBuilder rideUrlBuilder;

	@GetMapping("/ro/rides")
	public String ridesRo(
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) String date,
			@RequestParam(required = false) String packages,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "9") int size,
			Model model,
			HttpSession session) {
		Boolean packagesBoolean = null;
		if (packages != null && (packages.equals("on") || packages.equals("true"))) {
			packagesBoolean = true;
		}
		pageModelService.buildRidesPageModel(model, session, "ro", from, to, date, packagesBoolean, page, size);
		return "rides";
	}

	@GetMapping("/ru/rides")
	public String ridesRu(
			@RequestParam(required = false) String from,
			@RequestParam(required = false) String to,
			@RequestParam(required = false) String date,
			@RequestParam(required = false) String packages,
			@RequestParam(defaultValue = "0") int page,
			@RequestParam(defaultValue = "9") int size,
			Model model,
			HttpSession session) {
		Boolean packagesBoolean = null;
		if (packages != null && (packages.equals("on") || packages.equals("true"))) {
			packagesBoolean = true;
		}
		pageModelService.buildRidesPageModel(model, session, "ru", from, to, date, packagesBoolean, page, size);
		return "rides";
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
}


