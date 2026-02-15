package com.scutelnic.rutex.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class GoogleOAuth2FailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String language = resolveLanguage(request);
        String oauthCode = extractOauthErrorCode(exception);

        String encodedCode = URLEncoder.encode(oauthCode, StandardCharsets.UTF_8);
        response.sendRedirect("/" + language + "/login?oauthError=true&oauthCode=" + encodedCode);
    }

    private String extractOauthErrorCode(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof OAuth2AuthenticationException oauthEx && oauthEx.getError() != null) {
                return oauthEx.getError().getErrorCode();
            }
            if (current instanceof OAuth2AuthorizationException authEx && authEx.getError() != null) {
                return authEx.getError().getErrorCode();
            }
            current = current.getCause();
        }
        return "unknown";
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
}
