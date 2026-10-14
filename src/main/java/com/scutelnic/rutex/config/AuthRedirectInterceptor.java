package com.scutelnic.rutex.config;

import com.scutelnic.rutex.util.AuthRedirect;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
public class AuthRedirectInterceptor implements HandlerInterceptor {
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        if (modelAndView == null || !"GET".equals(request.getMethod())) return;
        String view = modelAndView.getViewName();
        if (view == null || !view.matches("redirect:/(ro|ru)/login")) return;
        String query = request.getQueryString();
        String target = AuthRedirect.safeTarget(request.getRequestURI() + (query == null ? "" : "?" + query));
        if (target != null) {
            modelAndView.setViewName(view + "?redirect=" + URLEncoder.encode(target, StandardCharsets.UTF_8));
        }
    }
}
