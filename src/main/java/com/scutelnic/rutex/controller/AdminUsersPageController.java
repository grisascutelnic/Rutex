package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.LocalityService;
import com.scutelnic.rutex.service.UserService;
import com.scutelnic.rutex.service.PageModelService;
import com.scutelnic.rutex.service.ReservationService;
import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.service.SiteVisitorService;
import com.scutelnic.rutex.service.GooglePlacesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.List;

@Controller
public class AdminUsersPageController {

	@Autowired
	private PageModelService pageModelService;

	@Autowired
	private SiteVisitorService siteVisitorService;

	@Autowired
	private RideService rideService;

	@Autowired
	private GooglePlacesService googlePlacesService;

	@Autowired
	private LocalityService localityService;

	@Autowired
	private UserService userService;

	@Autowired
	private ReservationService reservationService;

	@GetMapping("/ro/users")
	public String usersRo(Model model, HttpSession session, HttpServletRequest request) {
		return handleUsers("ro", model, session);
	}

	@GetMapping("/ru/users")
	public String usersRu(Model model, HttpSession session, HttpServletRequest request) {
		return handleUsers("ru", model, session);
	}

	private String handleUsers(String language, Model model, HttpSession session) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "users", language);
		pageModelService.setLanguageInModel(model, language);

		User currentUser = (User) session.getAttribute("user");
		if (currentUser == null) {
			return "redirect:/" + language + "/login";
		}
		boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().stream().anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
		if (!isAdmin) {
			return "redirect:/" + language;
		}


		try {
			// Obținem ultimii utilizatori și formatăm telefonul pentru afișare
			List<User> allUsers = userService.getRecentUsers();
			List<User> formattedUsers = allUsers.stream()
				.map(userService::getUserWithFormattedPhone)
				.collect(Collectors.toList());
			model.addAttribute("users", formattedUsers);
		} catch (Exception e) {
			model.addAttribute("users", java.util.Collections.emptyList());
		}

		try {
			model.addAttribute("reservations", reservationService.getRecentReservations());
		} catch (Exception e) {
			model.addAttribute("reservations", java.util.Collections.emptyList());
		}

		try {
			Map<String, Object> visitorStats = siteVisitorService.getStatistics();
			model.addAttribute("visitorStats", visitorStats);
		} catch (Exception ignored) {}

		try {
			Map<String, Object> rideStats = rideService.getRideStatistics();
			model.addAttribute("rideStats", rideStats);
		} catch (Exception ignored) {}

		try {
			Map<String, Object> googlePlacesStats = new HashMap<>();
			googlePlacesStats.putAll(googlePlacesService.getApiStatistics());
			googlePlacesStats.putAll(localityService.getSearchStatistics());
			model.addAttribute("googlePlacesStats", googlePlacesStats);
		} catch (Exception ignored) {}

		return "users";
	}
}


