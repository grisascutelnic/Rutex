package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.config.PhoneCompletionInterceptor;
import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.service.RideService;
import com.scutelnic.rutex.service.RideViewService;
import com.scutelnic.rutex.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PhoneVisibilityTest {
    @Test
    void oldSessionWithoutPhoneCanContinue() {
        var request = new MockHttpServletRequest();
        request.getSession().setAttribute("forcePhoneCompletion", true);
        assertTrue(new PhoneCompletionInterceptor().preHandle(request, new MockHttpServletResponse(), new Object()));
        assertNull(request.getSession().getAttribute("forcePhoneCompletion"));
    }

    @Test
    void contactPhoneRequiresConsentForTheMatchingAnnouncement() {
        var users = mock(UserService.class);
        var rides = mock(RideRepository.class);
        var controller = new UserController();
        ReflectionTestUtils.setField(controller, "userService", users);
        ReflectionTestUtils.setField(controller, "rideRepository", rides);
        var owner = new User();
        owner.setId(7L);
        owner.setPhone("+373 69123456");
        owner.setEmail("private@example.com");
        when(users.getUserById(7L)).thenReturn(Optional.of(owner));
        when(users.getUserWithFormattedPhone(owner)).thenReturn(owner);
        var ride = new Ride();
        ride.setUser(owner);
        when(rides.findById(1L)).thenReturn(Optional.of(ride));
        var session = new MockHttpSession();
        session.setAttribute("user", new User());
        assertNull(controller.getUserContact(7L, null, session).getBody().get("phone"));
        assertNull(controller.getUserContact(7L, 1L, session).getBody().get("phone"));
        ride.setShowPhoneNumber(true);
        assertEquals(owner.getPhone(), controller.getUserContact(7L, 1L, session).getBody().get("phone"));
        assertFalse(controller.getUserContact(7L, 1L, session).getBody().containsKey("email"));
        var otherOwner = new User();
        otherOwner.setId(8L);
        ride.setUser(otherOwner);
        assertNull(controller.getUserContact(7L, 1L, session).getBody().get("phone"));
    }

    @Test
    void rideApiHidesPhoneUntilExplicitConsentIncludingLegacyRows() {
        var service = new RideService();
        ReflectionTestUtils.setField(service, "rideViewService", mock(RideViewService.class));
        var owner = new User();
        owner.setId(7L);
        owner.setPhone("69123456");
        owner.setEmail("private@example.com");
        var ride = new Ride();
        ride.setUser(owner);
        ride.setShowPhoneNumber(null);
        RideDTO dto = ReflectionTestUtils.invokeMethod(service, "convertToDTO", ride);
        assertNull(dto.getDriverPhone());
        assertFalse(dto.getShowPhoneNumber());
        ride.setShowPhoneNumber(true);
        dto = ReflectionTestUtils.invokeMethod(service, "convertToDTO", ride);
        assertNotNull(dto.getDriverPhone());
        assertNull(dto.getDriverEmail());
        ride.setShowPhoneNumber(false);
        dto = ReflectionTestUtils.invokeMethod(service, "convertToDTO", ride);
        assertNull(dto.getDriverPhone());
    }
}
