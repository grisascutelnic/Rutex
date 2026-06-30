package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.AdminReservationDTO;
import com.scutelnic.rutex.dto.ReservationRequest;
import com.scutelnic.rutex.entity.Reservation;
import com.scutelnic.rutex.entity.Ride;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.repository.ReservationRepository;
import com.scutelnic.rutex.repository.RideRepository;
import com.scutelnic.rutex.util.RideUrlBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import jakarta.servlet.http.HttpServletRequest;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationService.class);

    @Value("${app.base-url:}")
    private String baseUrlConfig;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RideRepository rideRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private RideUrlBuilder rideUrlBuilder;

    public Reservation createReservation(ReservationRequest request, HttpServletRequest httpRequest) {
        validateRequest(request);

        Ride ride = rideRepository.findByIdWithRelations(request.getRideId())
            .orElseThrow(() -> new IllegalArgumentException("Cursa nu a fost găsită."));

        User driver = ride.getUser();
        if (driver == null) {
            throw new IllegalArgumentException("Șoferul nu a fost găsit.");
        }

        Reservation reservation = new Reservation();
        reservation.setRide(ride);
        reservation.setDriver(driver);
        reservation.setPassengerFirstName(request.getFirstName().trim());
        reservation.setPassengerLastName(request.getLastName().trim());
        reservation.setPassengerEmail(request.getEmail().trim());
        reservation.setPassengerPhone(request.getPhone().trim());
        reservation.setPassengerCount(request.getPassengerCount());
        reservation.setAdditionalInfo(normalizeOptional(request.getAdditionalInfo()));
        reservation.setConsentToShareData(Boolean.TRUE.equals(request.getConsentToShareData()));

        Reservation saved = reservationRepository.save(reservation);

        String language = "ru".equalsIgnoreCase(request.getLanguage()) ? "ru" : "ro";

        try {
            sendReservationEmails(saved, ride, driver, language, httpRequest);
        } catch (Exception ex) {
            logger.error("Reservation {} saved, but sending reservation emails failed", saved.getId(), ex);
        }

        try {
            sendDriverNotification(saved, ride, driver);
        } catch (Exception ex) {
            logger.error("Reservation {} saved, but sending driver notification failed", saved.getId(), ex);
        }

        return saved;
    }

    public List<AdminReservationDTO> getRecentReservations() {
        return reservationRepository.findRecentWithRideAndDriver(PageRequest.of(0, 200)).stream()
            .map(this::toAdminDto)
            .collect(Collectors.toList());
    }

    private AdminReservationDTO toAdminDto(Reservation reservation) {
        Ride ride = reservation.getRide();
        User driver = reservation.getDriver();

        String driverName = driver != null
            ? String.format("%s %s", safe(driver.getFirstName()), safe(driver.getLastName())).trim()
            : "";

        return new AdminReservationDTO(
            reservation.getId(),
            reservation.getCreatedAt(),
            ride != null ? ride.getId() : null,
            ride != null ? ride.getFromLocation() : "",
            ride != null ? ride.getToLocation() : "",
            ride != null ? ride.getTravelDate() : null,
            ride != null ? ride.getDepartureTime() : null,
            driver != null ? driver.getId() : null,
            driverName,
            driver != null ? driver.getEmail() : "",
            reservation.getPassengerFirstName(),
            reservation.getPassengerLastName(),
            reservation.getPassengerEmail(),
            reservation.getPassengerPhone(),
            reservation.getPassengerCount(),
            reservation.getAdditionalInfo(),
            reservation.getConsentToShareData()
        );
    }

    private void sendReservationEmails(Reservation reservation, Ride ride, User driver, String language, HttpServletRequest httpRequest) {
        String baseUrl = resolveBaseUrl(httpRequest);

        User formattedDriver = driver;
        if (driver != null && driver.getPhone() != null) {
            formattedDriver = userService.getUserWithFormattedPhone(driver);
        }

        String driverName = escapeHtml(String.format("%s %s", safe(formattedDriver.getFirstName()), safe(formattedDriver.getLastName())).trim());
        String driverEmail = escapeHtml(safe(formattedDriver.getEmail()));
        String driverPhone = escapeHtml(safe(formattedDriver.getPhone()));
        String vehicleInfo = escapeHtml(buildVehicleInfo(ride));

        String rideLink = baseUrl + rideUrlBuilder.buildRidePath(language, ride);
        String driverLink = baseUrl + "/" + language + "/profile/" + formattedDriver.getId();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ro", "RO"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", new Locale("ro", "RO"));

        String travelDate = ride.getTravelDate() != null ? ride.getTravelDate().format(dateFormatter) : "-";
        String departureTime = ride.getDepartureTime() != null ? ride.getDepartureTime().format(timeFormatter) : "-";

        String passengerName = escapeHtml(reservation.getPassengerFirstName() + " " + reservation.getPassengerLastName());
        String passengerEmail = escapeHtml(reservation.getPassengerEmail());
        String passengerPhone = escapeHtml(reservation.getPassengerPhone());

        String driverSubject = language.equals("ru")
            ? "Новый пассажир - Rutex"
            : "Pasager nou - Rutex";
        String driverContent = buildDriverEmailContent(language, passengerName, passengerEmail, passengerPhone,
            reservation.getPassengerCount(), reservation.getAdditionalInfo(), ride, travelDate, departureTime);

        String passengerSubject = language.equals("ru")
            ? "Информирование отправлено - Rutex"
            : "Informare trimisă - Rutex";
        String passengerContent = buildPassengerEmailContent(language, passengerName, driverName, driverEmail, driverPhone,
            vehicleInfo, ride, travelDate, departureTime, driverLink, rideLink);

        if (!driverEmail.isBlank()) {
            emailService.sendEmailAsync(driverEmail, driverSubject, driverContent);
        }
        if (!passengerEmail.isBlank()) {
            emailService.sendEmailAsync(passengerEmail, passengerSubject, passengerContent);
        }
    }

    private void sendDriverNotification(Reservation reservation, Ride ride, User driver) {
        if (driver == null) {
            return;
        }

        String route = safe(ride.getFromLocation()) + " -> " + safe(ride.getToLocation());
        String passengerName = reservation.getPassengerFirstName() + " " + reservation.getPassengerLastName();
        String countText = reservation.getPassengerCount() != null ? reservation.getPassengerCount().toString() : "-";
        String passengerPhone = reservation.getPassengerPhone() != null ? reservation.getPassengerPhone() : "-";
        String passengerEmail = reservation.getPassengerEmail() != null ? reservation.getPassengerEmail() : "-";

        String titleRo = "Pasager nou";
        String messageRo = String.format("Ai un pasager nou: %s, %s, %s locuri. Telefon: %s. Email: %s.",
            passengerName, route, countText, passengerPhone, passengerEmail);

        String titleRu = "Новый пассажир";
        String messageRu = String.format("Новый пассажир: %s, %s, мест: %s. Телефон: %s. Email: %s.",
            passengerName, route, countText, passengerPhone, passengerEmail);

        notificationService.createNotification(driver, titleRo, messageRo, titleRu, messageRu);
    }

    private String buildDriverEmailContent(String language, String passengerName, String passengerEmail, String passengerPhone,
                                           Integer passengerCount, String additionalInfo, Ride ride,
                                           String travelDate, String departureTime) {
        String route = escapeHtml(safe(ride.getFromLocation()) + " -> " + safe(ride.getToLocation()));
        String countText = passengerCount != null ? escapeHtml(passengerCount.toString()) : "-";
        String infoText = additionalInfo != null ? escapeHtml(additionalInfo) : "-";

        if ("ru".equals(language)) {
            return String.format("""
                <div class="header"><h1>Новый пассажир</h1></div>
                <p>Здравствуйте!</p>
                <p>У вас новый пассажир:</p>
                <div class="highlight">
                    <strong>Пассажир:</strong> %s<br>
                    <strong>Email:</strong> %s<br>
                    <strong>Телефон:</strong> %s<br>
                    <strong>Количество мест:</strong> %s<br>
                    <strong>Доп. информация:</strong> %s
                </div>
                <div class="highlight">
                    <strong>Маршрут:</strong> %s<br>
                    <strong>Дата:</strong> %s<br>
                    <strong>Время:</strong> %s
                </div>
                <div class="footer">Команда Rutex</div>
                """,
                passengerName, passengerEmail, passengerPhone, countText, infoText, route, travelDate, departureTime);
        }

        return String.format("""
            <div class="header"><h1>Pasager nou</h1></div>
            <p>Salut!</p>
            <p>Ai un pasager nou:</p>
            <div class="highlight">
                <strong>Pasager:</strong> %s<br>
                <strong>Email:</strong> %s<br>
                <strong>Telefon:</strong> %s<br>
                <strong>Număr persoane:</strong> %s<br>
                <strong>Info suplimentare:</strong> %s
            </div>
            <div class="highlight">
                <strong>Ruta:</strong> %s<br>
                <strong>Data:</strong> %s<br>
                <strong>Ora:</strong> %s
            </div>
            <div class="footer">Echipa Rutex</div>
            """,
            passengerName, passengerEmail, passengerPhone, countText, infoText, route, travelDate, departureTime);
    }

    private String buildPassengerEmailContent(String language, String passengerName, String driverName, String driverEmail,
                                              String driverPhone, String vehicleInfo, Ride ride, String travelDate,
                                              String departureTime, String driverLink, String rideLink) {
        String route = escapeHtml(safe(ride.getFromLocation()) + " -> " + safe(ride.getToLocation()));

        if ("ru".equals(language)) {
            return String.format("""
                <div class="header"><h1>Информирование отправлено</h1></div>
                <p>Здравствуйте, %s!</p>
                <p>Ваше информирование отправлено. Детали поездки:</p>
                <div class="highlight">
                    <strong>Водитель:</strong> %s<br>
                    <strong>Email:</strong> %s<br>
                    <strong>Телефон:</strong> %s<br>
                    <strong>Авто:</strong> %s
                </div>
                <div class="highlight">
                    <strong>Маршрут:</strong> %s<br>
                    <strong>Дата:</strong> %s<br>
                    <strong>Время:</strong> %s
                </div>
                <p>Профиль водителя: <a href="%s">%s</a></p>
                <p>Детали поездки: <a href="%s">%s</a></p>
                <p>После завершения поездки, пожалуйста, оставьте отзыв.</p>
                <div class="footer">Команда Rutex</div>
                """,
                passengerName, driverName, driverEmail, driverPhone, vehicleInfo, route, travelDate, departureTime,
                driverLink, driverLink, rideLink, rideLink);
        }

        return String.format("""
            <div class="header"><h1>Informare trimisă</h1></div>
            <p>Salut, %s!</p>
            <p>Informarea ta a fost trimisă. Detalii cursă:</p>
            <div class="highlight">
                <strong>Șofer:</strong> %s<br>
                <strong>Email:</strong> %s<br>
                <strong>Telefon:</strong> %s<br>
                <strong>Mașină:</strong> %s
            </div>
            <div class="highlight">
                <strong>Ruta:</strong> %s<br>
                <strong>Data:</strong> %s<br>
                <strong>Ora:</strong> %s
            </div>
            <p>Profil șofer: <a href="%s">%s</a></p>
            <p>Detalii cursă: <a href="%s">%s</a></p>
            <p>La finalul călătoriei, te rugăm să lași un rating.</p>
            <div class="footer">Echipa Rutex</div>
            """,
            passengerName, driverName, driverEmail, driverPhone, vehicleInfo, route, travelDate, departureTime,
            driverLink, driverLink, rideLink, rideLink);
    }

    private String buildVehicleInfo(Ride ride) {
        if (ride == null) {
            return "-";
        }

        String make = safe(ride.getVehicleMake());
        String color = safe(ride.getVehicleColor());
        String plate = safe(ride.getVehiclePlateNumber());

        java.util.List<String> parts = new java.util.ArrayList<>();
        if (!make.isBlank()) {
            parts.add(make);
        }
        if (!color.isBlank()) {
            parts.add(color);
        }
        if (!plate.isBlank()) {
            parts.add(plate);
        }

        String combined = String.join(" • ", parts);
        return combined.isBlank() ? "-" : combined;
    }

    private String buildBaseUrl(HttpServletRequest request) {
        String proto = optionalHeader(request, "X-Forwarded-Proto");
        if (proto.isBlank()) {
            proto = request.getScheme();
        }

        String host = optionalHeader(request, "X-Forwarded-Host");
        if (host.isBlank()) {
            host = request.getServerName();
        }

        String portHeader = optionalHeader(request, "X-Forwarded-Port");
        String portPart = "";

        if (!host.contains(":")) {
            if (!portHeader.isBlank()) {
                if (!("80".equals(portHeader) || "443".equals(portHeader))) {
                    portPart = ":" + portHeader;
                }
            } else {
                int port = request.getServerPort();
                if (("http".equalsIgnoreCase(proto) && port != 80) || ("https".equalsIgnoreCase(proto) && port != 443)) {
                    portPart = ":" + port;
                }
            }
        }

        return proto + "://" + host + portPart;
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        if (baseUrlConfig != null && !baseUrlConfig.trim().isEmpty()) {
            return baseUrlConfig.trim();
        }
        return buildBaseUrl(request);
    }

    private String optionalHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value == null ? "" : value.trim();
    }

    private void validateRequest(ReservationRequest request) {
        if (request == null || request.getRideId() == null) {
            throw new IllegalArgumentException("Cursa este obligatorie.");
        }
        if (isBlank(request.getFirstName())) {
            throw new IllegalArgumentException("Prenumele este obligatoriu.");
        }
        if (isBlank(request.getLastName())) {
            throw new IllegalArgumentException("Numele este obligatoriu.");
        }
        if (isBlank(request.getEmail())) {
            throw new IllegalArgumentException("Email-ul este obligatoriu.");
        }
        if (isBlank(request.getPhone())) {
            throw new IllegalArgumentException("Telefonul este obligatoriu.");
        }
        if (request.getPassengerCount() == null || request.getPassengerCount() < 1) {
            throw new IllegalArgumentException("Numărul de persoane trebuie să fie cel puțin 1.");
        }
        if (!Boolean.TRUE.equals(request.getConsentToShareData())) {
            throw new IllegalArgumentException("Este necesar acordul pentru transmiterea datelor către șofer.");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&#39;");
    }
}
