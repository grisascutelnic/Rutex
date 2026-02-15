package com.scutelnic.rutex.config;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PhoneCompletionInterceptor implements HandlerInterceptor {

    private final UserService userService;

    public PhoneCompletionInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return true;
        }

        Boolean forcePhoneCompletion = (Boolean) session.getAttribute("forcePhoneCompletion");
        if (!Boolean.TRUE.equals(forcePhoneCompletion)) {
            return true;
        }

        String path = request.getRequestURI();
        if (isAllowedPath(path)) {
            return true;
        }

        User freshUser = userService.getUserById(currentUser.getId()).orElse(currentUser);
        if (userService.hasPhoneNumber(freshUser)) {
            session.removeAttribute("forcePhoneCompletion");
            session.setAttribute("user", freshUser);
            return true;
        }

        if (path.startsWith("/api/")) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Completați numărul de telefon pentru a continua.\"}");
            return false;
        }

        String language = path.startsWith("/ru") ? "ru" : "ro";
        response.sendRedirect("/" + language + "/edit-profile?forcePhone=true");
        return false;
    }

    private boolean isAllowedPath(String path) {
        return path.startsWith("/ro/edit-profile")
                || path.startsWith("/ru/edit-profile")
                || path.startsWith("/api/users/update-profile")
                || path.startsWith("/api/auth/logout")
                || path.startsWith("/api/auth/user")
                || path.startsWith("/api/auth/check")
            || path.startsWith("/api/change-language")
            || path.startsWith("/api/translations/")
                || path.startsWith("/uploads/")
                || path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.startsWith("/images/")
                || path.startsWith("/favicon")
                || path.startsWith("/oauth2/")
                || path.startsWith("/login/oauth2/")
                || path.startsWith("/error");
    }
}
