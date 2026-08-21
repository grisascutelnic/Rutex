package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.entity.AnnouncementType;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.util.RideUrlBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageModelService {

	@Autowired
	private UserService userService;

	@Autowired
	private TranslationService translationService;

	@Autowired
	private RideService rideService;

	@Autowired
	private RecaptchaService recaptchaService;

	@Autowired
	private RideUrlBuilder rideUrlBuilder;

	@Value("${app.base-url:https://rutex.md}")
	private String baseUrl;

	public void addCurrentUserToModel(Model model, HttpSession session) {
		User sessionUser = (User) session.getAttribute("user");
		if (sessionUser != null) {
			try {
				User freshUser = userService.getUserById(sessionUser.getId()).orElse(null);
				if (freshUser != null) {
					model.addAttribute("currentUser", freshUser);
				} else {
					model.addAttribute("currentUser", sessionUser);
				}
			} catch (Exception e) {
				model.addAttribute("currentUser", sessionUser);
			}
		}
	}

	public void addTranslationsToModel(Model model, String pageName, String language) {
		try {
			Map<String, String> translations = new HashMap<>();
			if (!"ro".equals(language)) {
				translations = translationService.getPageTranslations("ro", language, pageName);
				try {
					Map<String, String> navbarTranslations = translationService.getPageTranslations("ro", language, "navbar");
					translations.putAll(navbarTranslations);
				} catch (Exception ignored) {}
				try {
					Map<String, String> footerTranslations = translationService.getPageTranslations("ro", language, "footer");
					translations.putAll(footerTranslations);
				} catch (Exception ignored) {}
			} else {
				try {
					translations = translationService.getPageTranslations("ro", "ro", pageName);
					try {
						Map<String, String> navbarTranslations = translationService.getPageTranslations("ro", "ro", "navbar");
						translations.putAll(navbarTranslations);
					} catch (Exception ignored) {}
					try {
						Map<String, String> footerTranslations = translationService.getPageTranslations("ro", "ro", "footer");
						translations.putAll(footerTranslations);
					} catch (Exception ignored) {}
				} catch (Exception ignored) {}
			}
			if ("index".equals(pageName)) {
				try {
					Map<String, String> ridesTranslations = translationService.getPageTranslations("ro", language, "rides");
					translations.putAll(ridesTranslations);
				} catch (Exception ignored) {}
			}
			model.addAttribute("translations", translations);
		} catch (Exception e) {
			model.addAttribute("translations", new HashMap<>());
		}
	}

	public void setLanguageInModel(Model model, String language) {
		model.addAttribute("currentLanguage", language);
		model.addAttribute("isRomanian", "ro".equals(language));
		model.addAttribute("isRussian", "ru".equals(language));
	}

	public void addRecaptchaToModel(Model model) {
		model.addAttribute("recaptchaEnabled", recaptchaService.isEnabled());
		model.addAttribute("recaptchaSiteKey", recaptchaService.getSiteKey());
	}

	public void buildRidesPageModel(Model model,
			HttpSession session,
			String language,
			String from,
			String to,
			String date,
			Boolean packages,
			int page,
			int size) {
		buildRidesPageModel(model, session, language, from, to, date, packages, page, size, null);
	}

	public void buildRidesPageModel(Model model,
			HttpSession session,
			String language,
			String from,
			String to,
			String date,
			Boolean packages,
			int page,
			int size,
			com.scutelnic.rutex.entity.AnnouncementType announcementType) {
		addCurrentUserToModel(model, session);
		addTranslationsToModel(model, "rides", language);
		setLanguageInModel(model, language);

		try {
			List<RideDTO> filteredRides;
			if ((from != null && !from.isEmpty()) || (to != null && !to.isEmpty()) || (date != null && !date.isEmpty())) {
				com.scutelnic.rutex.dto.SearchRideRequest searchRequest = new com.scutelnic.rutex.dto.SearchRideRequest();
				searchRequest.setFromLocation(from != null ? from : "");
				searchRequest.setToLocation(to != null ? to : "");
				if (date != null && !date.isEmpty()) {
					try {
						searchRequest.setTravelDate(LocalDate.parse(date));
					} catch (Exception e) {
						searchRequest.setTravelDate(null);
					}
				}
				searchRequest.setPassengers(null);
				searchRequest.setLuggage(null);
				filteredRides = rideService.searchRides(searchRequest);
			} else {
				filteredRides = rideService.getAllActiveRides();
			}

			if (packages != null && packages) {
				filteredRides = filteredRides.stream()
					.filter(ride -> Boolean.TRUE.equals(ride.getIsPackageOnly()) || Boolean.TRUE.equals(ride.getTransportAndPackages()))
					.collect(java.util.stream.Collectors.toList());
			}
			if (announcementType != null) {
				filteredRides = filteredRides.stream()
					.filter(ride -> announcementType.equals(ride.getAnnouncementType()))
					.collect(java.util.stream.Collectors.toList());
			}

			int totalRides = filteredRides.size();
			int totalPages = Math.max(1, (int) Math.ceil((double) totalRides / size));
			int startIndex = page * size;
			int endIndex = Math.min(startIndex + size, totalRides);
			List<RideDTO> pagedRides = startIndex <= endIndex ? filteredRides.subList(Math.min(startIndex, totalRides), Math.min(endIndex, totalRides)) : new ArrayList<>();

			model.addAttribute("recentRides", pagedRides);
			model.addAttribute("allRides", pagedRides);
			model.addAttribute("currentPage", page);
			model.addAttribute("totalPages", totalPages);
			model.addAttribute("totalRides", totalRides);
			model.addAttribute("hasNextPage", page < totalPages - 1);
			model.addAttribute("hasPreviousPage", page > 0);
			model.addAttribute("filterFrom", from != null ? from : "");
			model.addAttribute("filterTo", to != null ? to : "");
			model.addAttribute("filterDate", date != null ? date : "");
			model.addAttribute("filterPackages", packages != null ? packages : false);
			System.out.println("====== s-au incarcat ======");
		} catch (Exception e) {
			model.addAttribute("recentRides", new ArrayList<>());
			model.addAttribute("allRides", new ArrayList<>());
			model.addAttribute("currentPage", 0);
			model.addAttribute("totalPages", 0);
			model.addAttribute("totalRides", 0);
			model.addAttribute("hasNextPage", false);
			model.addAttribute("hasPreviousPage", false);
			model.addAttribute("filterFrom", "");
			model.addAttribute("filterTo", "");
			model.addAttribute("filterDate", "");
			model.addAttribute("filterPackages", false);
		}
	}

	public String buildRideDetailsPage(Model model, HttpSession session, HttpServletRequest request, String rideSlug) {
		String language = request.getRequestURI().contains("/ru/") ? "ru" : "ro";
		Long rideId = rideUrlBuilder.extractRideId(rideSlug);

		if (rideId == null) {
			return "redirect:/" + language + "/rides";
		}

		addCurrentUserToModel(model, session);
		setLanguageInModel(model, language);
		addTranslationsToModel(model, "rides", language);

		RideDTO ride;
		try {
			ride = rideService.getRideById(rideId);
		} catch (Exception e) {
			return "redirect:/" + language + "/rides";
		}

		if (ride == null) {
			return "redirect:/" + language + "/rides";
		}

		String canonicalPath = rideUrlBuilder.buildRidePath(language, ride);
		if (!request.getRequestURI().equals(canonicalPath)) {
			return "redirect:" + canonicalPath;
		}

		model.addAttribute("ride", ride);
		model.addAttribute("canonicalRideUrl", absoluteUrl(canonicalPath));
		model.addAttribute("rideSeoTitle", buildRideSeoTitle(ride, language));
		model.addAttribute("rideSeoDescription", buildRideSeoDescription(ride, language));
		model.addAttribute("rideShareImageUrl", absoluteUrl("/api/rides/" + ride.getId()
			+ "/share-image?language=" + language + "&v=2x"));

		try {
			User driver = userService.getUserById(ride.getUserId()).orElse(null);
			if (driver != null && driver.getPhone() != null) {
				User formattedDriver = userService.getUserWithFormattedPhone(driver);
				model.addAttribute("driver", formattedDriver);
			} else {
				model.addAttribute("driver", driver);
			}

			if (driver != null) {
				String maskedPhone = userService.maskPhoneForDisplay(driver.getPhonePrefix(), driver.getPhone());
				if (maskedPhone != null) {
					maskedPhone = maskedPhone.replace("(", "").replace(")", "");
				}
				model.addAttribute("driverMaskedPhone", maskedPhone);
				model.addAttribute("driverMaskedEmail", userService.maskEmailForDisplay(driver.getEmail()));
			} else {
				model.addAttribute("driverMaskedPhone", null);
				model.addAttribute("driverMaskedEmail", null);
			}
		} catch (Exception e) {
			model.addAttribute("driver", null);
			model.addAttribute("driverMaskedPhone", null);
			model.addAttribute("driverMaskedEmail", null);
		}

		return "ride-details";
	}

	private String buildRideSeoTitle(RideDTO ride, String language) {
		String route = shortLocation(ride.getFromLocation()) + " - " + shortLocation(ride.getToLocation());
		boolean passengerRequest = ride.getAnnouncementType() == AnnouncementType.PASSENGER_REQUEST;
		if ("ru".equals(language)) {
			return route + (passengerRequest ? " | Ищу поездку на Rutex" : " | Поездка на Rutex");
		}
		return route + (passengerRequest ? " | Cerere de cursă pe Rutex" : " | Cursă disponibilă pe Rutex");
	}

	private String buildRideSeoDescription(RideDTO ride, String language) {
		String route = shortLocation(ride.getFromLocation()) + " - " + shortLocation(ride.getToLocation());
		String date = formatRideDateTime(ride, language);
		boolean passengerRequest = ride.getAnnouncementType() == AnnouncementType.PASSENGER_REQUEST;
		Integer seatCount = passengerRequest ? ride.getRequestedSeats() : ride.getAvailableSeats();
		String seats = seatCount != null ? seatCount.toString() : "";

		if ("ru".equals(language)) {
			String description = passengerRequest
				? "Ищу транспорт по маршруту " + route + " на Rutex"
				: "Найдите поездку " + route + " на Rutex";
			if (!date.isBlank()) {
				description += ", " + date;
			}
			if (!seats.isBlank() && !Boolean.TRUE.equals(ride.getIsPackageOnly())) {
				description += passengerRequest ? ", пассажиров: " + seats : ", мест: " + seats;
			}
			return description + (passengerRequest
				? ". Посмотрите детали запроса на поездку."
				: ". Свяжитесь с водителем и проверьте детали маршрута.");
		}

		String description = passengerRequest
			? "Caut transport pe ruta " + route + " pe Rutex"
			: "Găsește cursa " + route + " pe Rutex";
		if (!date.isBlank()) {
			description += ", " + date;
		}
		if (!seats.isBlank() && !Boolean.TRUE.equals(ride.getIsPackageOnly())) {
			description += passengerRequest ? ", " + seats + " pasageri" : ", " + seats + " locuri disponibile";
		}
		return description + (passengerRequest
			? ". Vezi detaliile cererii de cursă."
			: ". Vezi detaliile rutei și contactează șoferul.");
	}

	private String shortLocation(String location) {
		if (location == null || location.isBlank()) {
			return "";
		}
		int commaIndex = location.indexOf(',');
		return commaIndex >= 0 ? location.substring(0, commaIndex).trim() : location.trim();
	}

	private String formatRideDateTime(RideDTO ride, String language) {
		if (ride.getDepartureTime() == null) {
			return "";
		}
		String date = ride.getDepartureTime().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
		if (Boolean.TRUE.equals(ride.getFlexibleTime())) {
			return date + ("ru".equals(language) ? ", гибкое время" : ", oră flexibilă");
		}
		return date + " " + ride.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm"));
	}

	private String absoluteUrl(String path) {
		String trimmedBaseUrl = baseUrl != null && !baseUrl.isBlank() ? baseUrl.trim() : "https://rutex.md";
		while (trimmedBaseUrl.endsWith("/")) {
			trimmedBaseUrl = trimmedBaseUrl.substring(0, trimmedBaseUrl.length() - 1);
		}
		if (path == null || path.isBlank()) {
			return trimmedBaseUrl;
		}
		return trimmedBaseUrl + (path.startsWith("/") ? path : "/" + path);
	}
}

