package com.scutelnic.rutex.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/** Clears the obsolete phone requirement from existing sessions. */
@Component
public class PhoneCompletionInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.removeAttribute("forcePhoneCompletion");
        }
        return true;
    }
}
