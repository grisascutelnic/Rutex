package com.scutelnic.rutex.config;

import com.scutelnic.rutex.controller.GoogleLoginController;
import com.scutelnic.rutex.controller.LanguageRedirectController;
import com.scutelnic.rutex.controller.PublicPageController;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.UserService;
import com.scutelnic.rutex.util.AuthRedirect;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

class AuthRedirectTest {
    @Test
    void protectedPagesKeepTheirDestinationAndQueryInBothLanguages() throws Exception {
        MockMvc mvc = MockMvcBuilders.standaloneSetup(new PublicPageController())
                .addInterceptors(new AuthRedirectInterceptor()).build();
        for (String language : new String[]{"ro", "ru"}) {
            for (String page : new String[]{"messages?userId=42", "edit-ride?id=7", "add-ride?type=passenger"}) {
                String target = "/" + language + "/" + page;
                mvc.perform(get(target)).andExpect(redirectedUrl("/" + language + "/login?redirect="
                        + URLEncoder.encode(target, StandardCharsets.UTF_8)));
            }
        }
    }

    @Test
    void googleLoginReturnsToConversationAndConsumesDestination() throws Exception {
        var session = new MockHttpSession();
        var controller = new GoogleLoginController();
        assertEquals("redirect:/oauth2/authorization/google", controller.startGoogleLogin("/ru/messages?userId=42", session));
        var service = mock(UserService.class);
        var user = new User();
        user.setPreferredLanguage("ro");
        when(service.findOrCreateGoogleUser("user@example.com", null, null, null)).thenReturn(user);
        var principal = mock(OAuth2User.class);
        when(principal.getAttribute("email")).thenReturn("user@example.com");
        var authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);
        var request = new MockHttpServletRequest();
        request.setSession(session);
        var response = new MockHttpServletResponse();
        var handler = new GoogleOAuth2SuccessHandler(service);
        handler.onAuthenticationSuccess(request, response, authentication);
        assertEquals("/ru/messages?userId=42", response.getRedirectedUrl());
        assertNull(session.getAttribute(AuthRedirect.SESSION_ATTRIBUTE));
        assertSame(user, session.getAttribute("user"));
        var nextResponse = new MockHttpServletResponse();
        handler.onAuthenticationSuccess(request, nextResponse, authentication);
        assertEquals("/ro", nextResponse.getRedirectedUrl());
    }

    @Test
    void languageRedirectPreservesExplicitDestination() {
        var request = new MockHttpServletRequest();
        request.setParameter("redirect", "/ro/messages?userId=42");
        var controller = new LanguageRedirectController();
        assertEquals("redirect:/ro/login?redirect=%2Fro%2Fmessages%3FuserId%3D42",
                controller.redirectLoginToLanguage(request, new MockHttpSession()));
    }

    @Test
    void rejectsExternalUrlsAndAuthenticationLoops() {
        for (String target : new String[]{"https://example.com", "//example.com", "/\\example.com", "/%2fexample.com",
                "/ro/login", "/ru/register?redirect=/ro", "/ro/forgot-password", "/oauth2/authorization/google",
                "/auth/google", "/ro/../ro/login", "/ro/messages\n"}) {
            assertNull(AuthRedirect.safeTarget(target), target);
        }
        assertEquals("/ru/messages?userId=42#chat", AuthRedirect.safeTarget("/ru/messages?userId=42#chat"));
        var session = new MockHttpSession();
        session.setAttribute(AuthRedirect.SESSION_ATTRIBUTE, "/ro/add-ride");
        new GoogleLoginController().startGoogleLogin("//example.com", session);
        assertNull(session.getAttribute(AuthRedirect.SESSION_ATTRIBUTE));
    }
}
