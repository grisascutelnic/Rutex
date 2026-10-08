package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.PageModelService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.ExtendedModelMap;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProfilePageControllerTest {
    @Test
    void ownPublicProfileRedirectsToPersonalProfileInBothLanguages() {
        var controller = new ProfilePageController();
        var session = new MockHttpSession();
        var user = new User();
        user.setId(7L);
        session.setAttribute("user", user);
        var model = new ExtendedModelMap();
        var request = new MockHttpServletRequest();
        assertEquals("redirect:/ro/profile", controller.userProfileRo(7L, model, session, request));
        assertEquals("redirect:/ru/profile", controller.userProfileRu(7L, model, session, request));
        assertFalse(model.containsAttribute("targetUserId"));
    }

    @Test
    void otherProfilesAndAnonymousVisitsKeepPublicProfile() {
        var controller = new ProfilePageController();
        ReflectionTestUtils.setField(controller, "pageModelService", mock(PageModelService.class));
        var session = new MockHttpSession();
        var model = new ExtendedModelMap();
        var request = new MockHttpServletRequest();
        assertEquals("profile", controller.userProfileRo(7L, model, session, request));
        assertEquals(7L, model.get("targetUserId"));
        var user = new User();
        user.setId(9L);
        session.setAttribute("user", user);
        assertEquals("profile", controller.userProfileRu(7L, model, session, request));
        assertEquals(7L, model.get("targetUserId"));
    }
}
