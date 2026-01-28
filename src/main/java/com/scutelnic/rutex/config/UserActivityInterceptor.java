package com.scutelnic.rutex.config;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.LocalDateTime;

@Component
public class UserActivityInterceptor implements HandlerInterceptor {

    private static final long UPDATE_INTERVAL_MS = 2 * 60 * 1000L;
    private static final String LAST_SEEN_SESSION_KEY = "lastSeenUpdateMs";

    private final UserService userService;

    public UserActivityInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return true;
        }

        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null || currentUser.getId() == null) {
            return true;
        }

        Long lastUpdateMs = (Long) session.getAttribute(LAST_SEEN_SESSION_KEY);
        long nowMs = System.currentTimeMillis();
        if (lastUpdateMs != null && (nowMs - lastUpdateMs) < UPDATE_INTERVAL_MS) {
            return true;
        }

        userService.updateLastSeenAt(currentUser.getId(), LocalDateTime.now());
        session.setAttribute(LAST_SEEN_SESSION_KEY, nowMs);
        return true;
    }
}
