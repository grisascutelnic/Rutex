package com.scutelnic.rutex.config;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserService userService;

    @Value("${app.session.remember-me.timeout:30d}")
    private String rememberMeTimeout;

    public GoogleOAuth2SuccessHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String language = resolveLanguage(request);

        if (!(authentication.getPrincipal() instanceof OAuth2User oauthUser)) {
            redirectWithOauthError(response, language, "invalid_principal");
            return;
        }

        String email = oauthUser.getAttribute("email");
        if (email == null || email.trim().isEmpty()) {
            redirectWithOauthError(response, language, "missing_email");
            return;
        }

        String firstName = oauthUser.getAttribute("given_name");
        String lastName = oauthUser.getAttribute("family_name");
        String pictureUrl = oauthUser.getAttribute("picture");

        if ((firstName == null || firstName.isBlank()) && oauthUser.getAttribute("name") != null) {
            String fullName = oauthUser.getAttribute("name");
            if (fullName != null && !fullName.isBlank()) {
                String[] parts = fullName.trim().split("\\s+", 2);
                firstName = parts[0];
                if (parts.length > 1) {
                    lastName = parts[1];
                }
            }
        }

        try {
            User user = userService.findOrCreateGoogleUser(email, firstName, lastName, pictureUrl);
            language = resolveLanguageFromUser(user, language);
            HttpSession session = request.getSession(true);
            session.setAttribute("user", user);
            session.setAttribute("currentLanguage", language);
            session.setMaxInactiveInterval(parseTimeoutToSeconds(rememberMeTimeout));

            if (!userService.hasPhoneNumber(user)) {
                session.setAttribute("forcePhoneCompletion", true);
                response.sendRedirect("/" + language + "/edit-profile?forcePhone=true");
                return;
            }

            session.removeAttribute("forcePhoneCompletion");
            response.sendRedirect("/" + language);
        } catch (RuntimeException ex) {
            redirectWithOauthError(response, language, buildErrorCode(ex));
        }
    }

    private void redirectWithOauthError(HttpServletResponse response, String language, String oauthCode) throws IOException {
        String encodedCode = URLEncoder.encode(oauthCode, StandardCharsets.UTF_8);
        response.sendRedirect("/" + language + "/login?oauthError=true&oauthCode=" + encodedCode);
    }

    private String buildErrorCode(RuntimeException ex) {
        String simpleName = ex.getClass().getSimpleName();
        if (simpleName == null || simpleName.isBlank()) {
            return "runtime_exception";
        }
        return simpleName.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private int parseTimeoutToSeconds(String timeout) {
        if (timeout == null || timeout.isEmpty()) {
            return 30 * 24 * 60 * 60;
        }

        String unit = timeout.substring(timeout.length() - 1).toLowerCase();
        int value = Integer.parseInt(timeout.substring(0, timeout.length() - 1));

        return switch (unit) {
            case "s" -> value;
            case "m" -> value * 60;
            case "h" -> value * 60 * 60;
            case "d" -> value * 24 * 60 * 60;
            default -> 30 * 24 * 60 * 60;
        };
    }

    private String resolveLanguage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object sessionLanguage = session.getAttribute("currentLanguage");
            if (sessionLanguage instanceof String lang && ("ro".equals(lang) || "ru".equals(lang))) {
                return lang;
            }
        }

        String referer = request.getHeader("Referer");
        if (referer != null && referer.contains("/ru/")) {
            return "ru";
        }
        return "ro";
    }

    private String resolveLanguageFromUser(User user, String fallbackLanguage) {
        if (user == null || user.getPreferredLanguage() == null) {
            return fallbackLanguage;
        }
        String preferredLanguage = user.getPreferredLanguage().trim().toLowerCase().replace('_', '-');
        if (preferredLanguage.equals("ru") || preferredLanguage.startsWith("ru-")) {
            return "ru";
        }
        return "ro";
    }
}
