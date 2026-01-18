package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.service.PageModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class ProfilePageController {

	@Autowired
	private PageModelService pageModelService;

	@GetMapping("/ro/profile")
	public String profileRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "profile", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "profile";
	}

	@GetMapping("/ru/profile")
	public String profileRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "profile", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "profile";
	}

	@GetMapping("/ro/profile/{userId}")
	public String userProfileRo(@PathVariable Long userId, Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "profile", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		model.addAttribute("targetUserId", userId);
		return "profile";
	}

	@GetMapping("/ru/profile/{userId}")
	public String userProfileRu(@PathVariable Long userId, Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "profile", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		model.addAttribute("targetUserId", userId);
		return "profile";
	}

	@GetMapping("/ro/edit-profile")
	public String editProfileRo(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "edit-profile", "ro");
		pageModelService.setLanguageInModel(model, "ro");
		return "edit-profile";
	}

	@GetMapping("/ru/edit-profile")
	public String editProfileRu(Model model, HttpSession session, HttpServletRequest request) {
		pageModelService.addCurrentUserToModel(model, session);
		pageModelService.addTranslationsToModel(model, "edit-profile", "ru");
		pageModelService.setLanguageInModel(model, "ru");
		return "edit-profile";
	}
}


