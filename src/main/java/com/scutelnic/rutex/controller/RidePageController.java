package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.PageModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class RidePageController {

	@Autowired
	private PageModelService pageModelService;

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

	@GetMapping("/ro/ride/{id}")
	public String rideDetailsRo(@PathVariable Long id, Model model, HttpSession session, HttpServletRequest request) {
		return pageModelService.buildRideDetailsPage(model, session, request, id);
	}

	@GetMapping("/ru/ride/{id}")
	public String rideDetailsRu(@PathVariable Long id, Model model, HttpSession session, HttpServletRequest request) {
		return pageModelService.buildRideDetailsPage(model, session, request, id);
	}
}


