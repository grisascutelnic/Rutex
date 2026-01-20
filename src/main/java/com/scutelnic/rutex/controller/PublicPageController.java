package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.PageModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class PublicPageController {

	@Autowired
	private PageModelService pageModelService;

	@GetMapping("/ro")
	public String indexRo(@RequestParam(value = "from", required = false) String from,
						  @RequestParam(value = "to", required = false) String to,
						  @RequestParam(value = "date", required = false) String date,
						  @RequestParam(value = "packages", required = false) Boolean packages,
						  @RequestParam(value = "page", required = false, defaultValue = "0") int page,
						  @RequestParam(value = "size", required = false, defaultValue = "9") int size,
						  Model model, HttpSession session, HttpServletRequest request) {

		pageModelService.buildRidesPageModel(model, session, "ro", from, to, date, packages, page, size);
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "index", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "index";
	}

	@GetMapping("/ru")
	public String indexRu(@RequestParam(value = "from", required = false) String from,
						  @RequestParam(value = "to", required = false) String to,
						  @RequestParam(value = "date", required = false) String date,
						  @RequestParam(value = "packages", required = false) Boolean packages,
						  @RequestParam(value = "page", required = false, defaultValue = "0") int page,
						  @RequestParam(value = "size", required = false, defaultValue = "9") int size,
						  Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.buildRidesPageModel(model, session, "ru", from, to, date, packages, page, size);
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "index", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "index";
	}

	@GetMapping("/ro/about")
	public String aboutRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "about", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "about";
	}

	@GetMapping("/ru/about")
	public String aboutRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "about", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "about";
	}

	@GetMapping("/ro/contact")
	public String contactRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "contact", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "contact";
	}

	@GetMapping("/ru/contact")
	public String contactRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "contact", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "contact";
	}

	@GetMapping("/ro/terms")
	public String termsRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "terms", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "terms";
	}

	@GetMapping("/ru/terms")
	public String termsRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "terms", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "terms";
	}

	@GetMapping("/ro/privacy")
	public String privacyRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "privacy", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "privacy";
	}

	@GetMapping("/ru/privacy")
	public String privacyRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "privacy", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "privacy";
	}

	@GetMapping("/ro/login")
	public String loginRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "login", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "login";
	}

	@GetMapping("/ru/login")
	public String loginRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "login", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "login";
	}

	@GetMapping("/ro/register")
	public String registerRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "register", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "register";
	}

	@GetMapping("/ru/register")
	public String registerRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "register", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "register";
	}

	@GetMapping("/ro/add-ride")
	public String addRideRo(Model model, HttpSession session, HttpServletRequest request) {
		User currentUser = (User) session.getAttribute("user");
		if (currentUser == null) { return "redirect:/ro/login"; }
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "add-ride", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "add-ride";
	}

	@GetMapping("/ru/add-ride")
	public String addRideRu(Model model, HttpSession session, HttpServletRequest request) {
		User currentUser = (User) session.getAttribute("user");
		if (currentUser == null) { return "redirect:/ru/login"; }
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "add-ride", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "add-ride";
	}

	@GetMapping("/ro/edit-ride")
	public String editRideRo(Model model, HttpSession session, HttpServletRequest request) {
		User currentUser = (User) session.getAttribute("user");
		if (currentUser == null) { return "redirect:/ro/login"; }
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "edit-ride", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "edit-ride";
	}

	@GetMapping("/ru/edit-ride")
	public String editRideRu(Model model, HttpSession session, HttpServletRequest request) {
		User currentUser = (User) session.getAttribute("user");
		if (currentUser == null) { return "redirect:/ru/login"; }
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "edit-ride", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "edit-ride";
	}

	@GetMapping("/ro/forgot-password")
	public String forgotPasswordRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "forgot-password", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "forgot-password";
	}

	@GetMapping("/ru/forgot-password")
	public String forgotPasswordRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "forgot-password", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "forgot-password";
	}

	@GetMapping("/reset-password")
	public String resetPassword(@RequestParam String token, Model model, HttpSession session, HttpServletRequest request) {
		model.addAttribute("token", token);
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "reset-password", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "reset-password";
	}
}


