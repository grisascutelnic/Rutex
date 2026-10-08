package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.AddRideRequest;
import com.scutelnic.rutex.entity.*;
import com.scutelnic.rutex.repository.*;
import com.scutelnic.rutex.util.RideUrlBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RideContactAndFlexibleTimeTest {
    private RideService service;
    private RideRepository rides;
    private UserRepository users;
    private User owner;
    private Ride existing;
    private AddRideRequest request;
    private VehicleService vehicles;

    @BeforeEach
    void setup() {
        service = new RideService();
        rides = mock(RideRepository.class);
        users = mock(UserRepository.class);
        vehicles = mock(VehicleService.class);
        ReflectionTestUtils.setField(service, "vehicleService", vehicles);
        ReflectionTestUtils.setField(service, "rideRepository", rides);
        ReflectionTestUtils.setField(service, "userRepository", users);
        ReflectionTestUtils.setField(service, "rideViewService", mock(RideViewService.class));
        ReflectionTestUtils.setField(service, "routeSeoContentService", mock(RouteSeoContentService.class));
        ReflectionTestUtils.setField(service, "eventPublisher", mock(ApplicationEventPublisher.class));
        var urls = mock(RideUrlBuilder.class);
        when(urls.buildRidePath(anyString(), any(Ride.class))).thenReturn("/ro/ride/test-1");
        ReflectionTestUtils.setField(service, "rideUrlBuilder", urls);
        owner = new User();
        owner.setId(7L);
        when(users.findByIdForUpdate(7L)).thenReturn(Optional.of(owner));
        existing = new Ride();
        existing.setId(1L);
        existing.setUser(owner);
        existing.setAnnouncementType(AnnouncementType.PASSENGER_REQUEST);
        when(rides.findById(1L)).thenReturn(Optional.of(existing));
        when(rides.findActiveByUserId(7L)).thenReturn(List.of());
        when(rides.save(any(Ride.class))).thenAnswer(invocation -> {
            Ride ride = invocation.getArgument(0);
            ride.setId(1L);
            return ride;
        });
        request = new AddRideRequest();
        request.setAnnouncementType(AnnouncementType.PASSENGER_REQUEST);
        request.setFromLocation("Chișinău");
        request.setToLocation("Bălți");
        request.setTravelDate(LocalDate.now().plusDays(1));
        request.setDepartureTime(LocalTime.of(12, 30));
        request.setAvailableSeats(0);
        request.setRequestedSeats(1);
    }

    @Test
    void creationWithConsentButNoPhoneIsRejected() {
        request.setShowPhoneNumber(true);
        assertThrows(IllegalArgumentException.class, () -> service.addRide(request, owner));
        verify(rides, never()).save(any());
    }

    @Test
    void editingWithConsentButNoPhoneIsRejected() {
        request.setShowPhoneNumber(true);
        assertThrows(IllegalArgumentException.class, () -> service.updateRide(1L, request, owner));
        verify(rides, never()).save(any());
    }

    @Test
    void creationStoresInlinePhoneInProfileAndAnnouncement() {
        request.setShowPhoneNumber(true);
        request.setContactPhone("+373 69 123 456");
        var result = service.addRide(request, owner);
        assertTrue(result.ride().getShowPhoneNumber());
        assertNotNull(result.ride().getDriverPhone());
        assertEquals("+37369123456", owner.getPhone());
        verify(users).save(owner);
    }

    @Test
    void editingSavesInlinePhoneAndKeepsFlexibleTime() {
        request.setShowPhoneNumber(true);
        request.setContactPhone("069 123 456");
        request.setFlexibleTime(true);
        request.setDepartureTime(null);
        var result = service.updateRide(1L, request, owner);
        assertTrue(result.getFlexibleTime());
        assertTrue(result.getShowPhoneNumber());
        assertEquals(LocalTime.MIDNIGHT, existing.getDepartureTime().toLocalTime());
        assertEquals("+373", owner.getPhonePrefix());
        assertEquals("69123456", owner.getPhone());
    }

    @Test
    void invalidInlinePhoneIsRejectedButUncheckedConsentNeedsNoPhone() {
        request.setShowPhoneNumber(true);
        request.setContactPhone("123");
        assertThrows(IllegalArgumentException.class, () -> service.updateRide(1L, request, owner));
        request.setShowPhoneNumber(false);
        assertFalse(service.updateRide(1L, request, owner).getShowPhoneNumber());
        verify(users, never()).save(any());
    }

    @Test
    void editingRetainsEveryFieldAndTheSavedVehicleSnapshot() {
        var vehicle = new Vehicle();
        vehicle.setId(12L);
        existing.setVehicle(vehicle);
        existing.setVehicleMake("Toyota Corolla");
        existing.setVehicleColor("Alb");
        existing.setVehiclePlateNumber("ABC 123");
        owner.setPhone("69123456");
        owner.setPhonePrefix("+373");
        request.setAnnouncementType(AnnouncementType.DRIVER_OFFER);
        request.setAvailableSeats(3);
        request.setVehicleId(12L);
        request.setDescription("Bagaj mare, oprire la aeroport.");
        request.setShowPhoneNumber(true);
        request.setTransportAndPackages(true);
        var result = service.updateRide(1L, request, owner);
        assertEquals(request.getFromLocation(), result.getFromLocation());
        assertEquals(request.getToLocation(), result.getToLocation());
        assertEquals(request.getTravelDate(), result.getTravelDate().toLocalDate());
        assertEquals(request.getDepartureTime(), result.getDepartureTime().toLocalTime());
        assertEquals(3, result.getAvailableSeats());
        assertEquals(request.getDescription(), result.getDescription());
        assertTrue(result.getShowPhoneNumber());
        assertTrue(result.getTransportAndPackages());
        assertEquals(12L, result.getVehicleId());
        assertEquals("Toyota Corolla", result.getVehicleMake());
        assertEquals("Alb", result.getVehicleColor());
        assertEquals("ABC 123", result.getVehiclePlateNumber());
        assertNull(result.getRequestedSeats());
        verifyNoInteractions(vehicles);
    }

    @Test
    void packageOnlyEditingKeepsDeletedVehicleSnapshotAndClearsPassengerFields() {
        existing.setVehicleMake("Toyota");
        existing.setVehicleColor("Alb");
        existing.setVehiclePlateNumber("ABC 123");
        request.setAnnouncementType(AnnouncementType.DRIVER_OFFER);
        request.setIsPackageOnly(true);
        request.setTransportAndPackages(true);
        var result = service.updateRide(1L, request, owner);
        assertTrue(result.getIsPackageOnly());
        assertFalse(result.getTransportAndPackages());
        assertEquals(0, result.getAvailableSeats());
        assertNull(result.getRequestedSeats());
        assertEquals("ABC 123", result.getVehiclePlateNumber());
    }

    @Test
    void changingToPassengerRequestClearsVehicleAndPackageFields() {
        existing.setVehicle(new Vehicle());
        existing.setVehicleMake("Toyota");
        existing.setVehicleColor("Alb");
        existing.setVehiclePlateNumber("ABC 123");
        request.setIsPackageOnly(true);
        request.setTransportAndPackages(true);
        request.setAvailableSeats(3);
        request.setRequestedSeats(4);
        var result = service.updateRide(1L, request, owner);
        assertEquals(4, result.getRequestedSeats());
        assertEquals(0, result.getAvailableSeats());
        assertNull(result.getVehicleId());
        assertNull(result.getVehicleMake());
        assertNull(result.getVehicleColor());
        assertNull(result.getVehiclePlateNumber());
        assertFalse(result.getIsPackageOnly());
        assertFalse(result.getTransportAndPackages());
    }

    @Test
    void editingSelectsNewVehicleAndUpdatesItsSnapshot() {
        var vehicle = new Vehicle();
        vehicle.setId(99L);
        vehicle.setMake("Dacia");
        vehicle.setColor("Alb");
        vehicle.setPlateNumber("NEW 777");
        when(vehicles.getVehicleForUser(99L, owner)).thenReturn(vehicle);
        request.setAnnouncementType(AnnouncementType.DRIVER_OFFER);
        request.setAvailableSeats(2);
        request.setVehicleId(99L);
        var result = service.updateRide(1L, request, owner);
        assertEquals(99L, result.getVehicleId());
        assertEquals("Dacia", result.getVehicleMake());
        assertEquals("Alb", result.getVehicleColor());
        assertEquals("NEW 777", result.getVehiclePlateNumber());
    }

    @Test
    void invalidEditDoesNotOverwriteTheExistingRide() {
        existing.setFromLocation("Ruta existentă");
        request.setRequestedSeats(0);
        assertThrows(IllegalArgumentException.class, () -> service.updateRide(1L, request, owner));
        assertEquals("Ruta existentă", existing.getFromLocation());
        verify(rides, never()).save(any());
    }

    @Test
    void moderatorCanKeepTheOwnersVehicleWithoutReplacingItWithTheirOwn() {
        var vehicle = new Vehicle();
        vehicle.setId(12L);
        existing.setVehicle(vehicle);
        existing.setVehicleMake("Toyota");
        existing.setVehiclePlateNumber("ABC 123");
        var moderator = new User();
        moderator.setId(9L);
        var role = new Role();
        role.setName("ROLE_MOD");
        moderator.setRoles(List.of(role));
        request.setAnnouncementType(AnnouncementType.DRIVER_OFFER);
        request.setVehicleId(12L);
        request.setAvailableSeats(2);
        var result = service.updateRide(1L, request, moderator);
        assertEquals(7L, result.getUserId());
        assertEquals(12L, result.getVehicleId());
        assertEquals("ABC 123", result.getVehiclePlateNumber());
        verifyNoInteractions(vehicles);
    }
}
