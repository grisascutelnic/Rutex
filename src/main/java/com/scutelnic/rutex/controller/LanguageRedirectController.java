package com.scutelnic.rutex.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class LanguageRedirectController {

	@GetMapping("/")
	public String redirectToRo() {
		return "redirect:/ro";
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


