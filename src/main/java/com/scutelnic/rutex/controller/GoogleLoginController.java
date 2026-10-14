package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.util.AuthRedirect;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class GoogleLoginController {
    @GetMapping("/auth/google")
    public String startGoogleLogin(@RequestParam(required = false) String redirect, HttpSession session) {
        String target = AuthRedirect.safeTarget(redirect);
        session.removeAttribute(AuthRedirect.SESSION_ATTRIBUTE);
        if (target != null) {
            session.setAttribute(AuthRedirect.SESSION_ATTRIBUTE, target);
        }
        return "redirect:/oauth2/authorization/google";
    }
}
