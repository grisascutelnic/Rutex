package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.User;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LanguageRedirectController {

	@GetMapping("/")
	public String redirectToPreferred(HttpServletRequest request, HttpSession session) {
		return "redirect:/" + detectLanguage(request, session);
	}

	@GetMapping("/login")
	public String redirectLoginToLanguage(HttpServletRequest request, HttpSession session) {
		String language = detectLanguage(request, session);
		return "redirect:/" + language + "/login";
	}

	@GetMapping("/register")
	public String redirectRegisterToLanguage(HttpServletRequest request, HttpSession session) {
		String language = detectLanguage(request, session);
		return "redirect:/" + language + "/register";
	}

	private String detectLanguage(HttpServletRequest request, HttpSession session) {
		String language = "ro";
		Object sessionUser = session.getAttribute("user");
		if (sessionUser instanceof User user && user.getPreferredLanguage() != null) {
			String preferredLanguage = user.getPreferredLanguage().trim().toLowerCase().replace('_', '-');
			if (preferredLanguage.equals("ru") || preferredLanguage.startsWith("ru-")) {
				return "ru";
			}
			return "ro";
		}

		Object sessionLanguage = session.getAttribute("currentLanguage");
		if (sessionLanguage instanceof String s && ("ro".equals(s) || "ru".equals(s))) {
			language = s;
		} else {
			String referer = request.getHeader("Referer");
			if (referer != null) {
				if (referer.contains("/ru/")) {
					language = "ru";
				} else if (referer.contains("/ro/")) {
					language = "ro";
				}
			}
		}
		return language;
	}
}


