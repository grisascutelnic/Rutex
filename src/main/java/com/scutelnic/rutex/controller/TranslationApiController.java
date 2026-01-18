package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.ChangeLanguageResponse;
import com.scutelnic.rutex.service.TranslationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.LocaleResolver;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Locale;
import java.util.Map;

@RestController
public class TranslationApiController {

	@Autowired
	private TranslationService translationService;

	@Autowired
	private LocaleResolver localeResolver;

	@PostMapping("/api/change-language")
	public ChangeLanguageResponse changeLanguage(
			@RequestParam String language,
			HttpServletRequest request,
			HttpServletResponse response,
			HttpSession session) {
		Locale newLocale = Locale.forLanguageTag(language);
		localeResolver.setLocale(request, response, newLocale);

		// Save language in session for redirects
		session.setAttribute("currentLanguage", language);

		String currentPath = request.getHeader("Referer");
		String redirectUrl;

		if (currentPath != null) {
			String scheme = request.getScheme();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();

			String baseUrl;
			if (("http".equals(scheme) && serverPort == 80) || ("https".equals(scheme) && serverPort == 443)) {
				baseUrl = scheme + "://" + serverName;
			} else {
				baseUrl = scheme + "://" + serverName + ":" + serverPort;
			}

			String path = currentPath.substring(currentPath.indexOf(serverName) + serverName.length());
			if (path.contains(":")) {
				path = path.substring(path.indexOf("/"));
			}

			if (path.startsWith("/ro/") || path.startsWith("/ru/")) {
				redirectUrl = baseUrl + "/" + language + path.substring(3);
			} else if (path.equals("/ro") || path.equals("/ru")) {
				redirectUrl = baseUrl + "/" + language;
			} else {
				redirectUrl = baseUrl + "/" + language + path;
			}
		} else {
			String scheme = request.getScheme();
			String serverName = request.getServerName();
			int serverPort = request.getServerPort();

			String baseUrl;
			if (("http".equals(scheme) && serverPort == 80) || ("https".equals(scheme) && serverPort == 443)) {
				baseUrl = scheme + "://" + serverName;
			} else {
				baseUrl = scheme + "://" + serverName + ":" + serverPort;
			}

			redirectUrl = baseUrl + "/" + language;
		}

		System.out.println("Language change: " + currentPath + " -> " + redirectUrl);
		return new ChangeLanguageResponse(redirectUrl);
	}

	@GetMapping("/api/translations/{pageName}")
	public Map<String, String> getTranslations(
			@PathVariable String pageName,
			@RequestParam String sourceLang,
			@RequestParam String targetLang) {
		return translationService.getPageTranslations(sourceLang, targetLang, pageName);
	}
}


