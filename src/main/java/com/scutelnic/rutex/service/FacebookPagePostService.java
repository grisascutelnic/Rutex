package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.RideDTO;
import com.scutelnic.rutex.entity.AnnouncementType;
import com.scutelnic.rutex.util.RideUrlBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Map;
import javax.imageio.ImageIO;

@Service
public class FacebookPagePostService {

    private final RestTemplate restTemplate;
    private final RideUrlBuilder rideUrlBuilder;

    @Value("${facebook.page-id:}")
    private String pageId;

    @Value("${facebook.page-access-token:}")
    private String pageAccessToken;

    @Value("${facebook.api.base-url:https://graph.facebook.com}")
    private String apiBaseUrl;

    @Value("${facebook.api.version:}")
    private String apiVersion;

    @Value("${app.base-url:}")
    private String baseUrlConfig;

    @Value("${facebook.image.width:2400}")
    private int imageWidth;

    @Value("${facebook.image.height:1260}")
    private int imageHeight;

    @Value("${facebook.post.enabled:true}")
    private boolean postEnabled;

    @Value("${facebook.post.text-format-enabled:false}")
    private boolean textFormatEnabled;

    @Value("${facebook.post.text-format-preset-id:}")
    private String textFormatPresetId;

    @Value("${facebook.post.text-format-max-length:0}")
    private int textFormatMaxLength;

    private Font robotoRegular;
    private Font robotoBlack;

    public FacebookPagePostService(RestTemplate restTemplate, RideUrlBuilder rideUrlBuilder) {
        this.restTemplate = restTemplate;
        this.rideUrlBuilder = rideUrlBuilder;
    }

    public String postRideToPage(RideDTO ride, String language, HttpServletRequest request) {
        validateConfig();

        String normalizedLanguage = "ru".equalsIgnoreCase(language) ? "ru" : "ro";
        String baseUrl = resolveBaseUrl(request);
        String rideLink = baseUrl + rideUrlBuilder.buildRidePath(normalizedLanguage, ride);
        String message = buildMessage(ride, normalizedLanguage, rideLink);
        String formattedMessage = applyTextFormatIfEnabled(message);

        MultiValueMap<String, Object> payload = new LinkedMultiValueMap<>();
        payload.add("message", formattedMessage);
        payload.add("access_token", pageAccessToken.trim());
        if (textFormatEnabled && textFormatPresetId != null && !textFormatPresetId.trim().isEmpty()) {
            payload.add("text_format_preset_id", textFormatPresetId.trim());
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(payload, headers);
        if (!postEnabled) {
            return "dry-run";
        }

        ResponseEntity<Map> response = restTemplate.postForEntity(buildApiUrl(), requestEntity, Map.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Publicarea pe Facebook a eșuat. Cod: " + response.getStatusCode());
        }

        Map body = response.getBody();
        if (body != null && body.get("id") != null) {
            return body.get("id").toString();
        }

        return "";
    }

    public byte[] generatePostImage(RideDTO ride, String language, HttpServletRequest request) {
        String normalizedLanguage = "ru".equalsIgnoreCase(language) ? "ru" : "ro";
        String baseUrl = resolveBaseUrl(request);
        String rideLink = baseUrl + rideUrlBuilder.buildRidePath(normalizedLanguage, ride);
        return buildPostImage(ride, normalizedLanguage, rideLink);
    }

    private void validateConfig() {
        if (pageId == null || pageId.trim().isEmpty()) {
            throw new IllegalStateException("facebook.page-id nu este configurat.");
        }
        if (pageAccessToken == null || pageAccessToken.trim().isEmpty()) {
            throw new IllegalStateException("facebook.page-access-token nu este configurat.");
        }
    }

    private String buildApiUrl() {
        String base = apiBaseUrl != null ? apiBaseUrl.trim() : "https://graph.facebook.com";
        if (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }

        String version = apiVersion != null ? apiVersion.trim() : "";
        if (!version.isEmpty()) {
            if (version.startsWith("/")) {
                version = version.substring(1);
            }
            return base + "/" + version + "/" + pageId + "/feed";
        }

        return base + "/" + pageId + "/feed";
    }

    private String buildMessage(RideDTO ride, String language, String rideLink) {
        String fromLocation = safe(ride.getFromLocation());
        String toLocation = safe(ride.getToLocation());
        boolean isRu = "ru".equals(language);

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ro", "RO"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", new Locale("ro", "RO"));
        String travelDate = ride.getTravelDate() != null ? ride.getTravelDate().format(dateFormatter) : "-";
        String departureTime = Boolean.TRUE.equals(ride.getFlexibleTime())
            ? (isRu ? "Гибкое время" : "Oră flexibilă")
            : (ride.getDepartureTime() != null ? ride.getDepartureTime().format(timeFormatter) : "-");

        boolean isPackageOnly = Boolean.TRUE.equals(ride.getIsPackageOnly());
        boolean transportAndPackages = Boolean.TRUE.equals(ride.getTransportAndPackages());
        boolean passengerRequest = isPassengerRequest(ride);
        String description = safe(ride.getDescription());

        StringBuilder message = new StringBuilder();
        if (passengerRequest) {
            message.append(isRu ? "🚗 Кто-нибудь едет?" : "🚗 Are cineva drum?")
                .append(System.lineSeparator())
                .append(fromLocation).append(" -> ").append(toLocation).append(".");
        } else if (isRu) {
            message.append("🚗 Еду:").append(System.lineSeparator())
                .append(fromLocation).append(" -> ").append(toLocation).append(".");
        } else {
            message.append("🚗 Am drum:").append(System.lineSeparator())
                .append(fromLocation).append(" -> ").append(toLocation).append(".");
        }
        message.append(System.lineSeparator());
        if (isRu) {
            message.append("📅 Дата: ").append(travelDate).append(" время: ").append(departureTime);
        } else {
            message.append("📅 Data: ").append(travelDate).append(" ora: ").append(departureTime);
        }
        message.append(System.lineSeparator());
        if (passengerRequest) {
            int requestedSeats = ride.getRequestedSeats() != null ? ride.getRequestedSeats() : 1;
            message.append(isRu ? "👥 Пассажиров: " : "👥 Pasageri care caută transport: ")
                .append(requestedSeats);
        } else if (isRu) {
            message.append("🚚 Транспорт ")
                .append(isPackageOnly ? "только посылки" : (transportAndPackages ? "пассажиры + посылки" : "пассажиры"));
        } else {
            message.append("🚚 Transport ")
                .append(isPackageOnly ? "doar colete" : (transportAndPackages ? "pasageri + colete" : "pasageri"));
        }

        if (!description.isBlank()) {
            message.append(System.lineSeparator()).append("📝 ").append(description);
        }

        String contactText = passengerRequest
            ? (isRu ? "👇 Чтобы связаться с пассажиром, нажмите здесь" : "👇 Pentru a contacta pasagerul apasă aici")
            : (isRu ? "👇 Чтобы связаться с водителем, нажмите здесь" : "👇 Pentru a contacta șoferul apasă aici");
        message.append(System.lineSeparator())
            .append(contactText)
            .append(System.lineSeparator())
            .append("🔗 ").append(rideLink);
        return message.toString();
    }

    private String applyTextFormatIfEnabled(String message) {
        if (!textFormatEnabled || textFormatMaxLength <= 0 || message == null || message.length() <= textFormatMaxLength) {
            return message;
        }
        int max = Math.max(0, textFormatMaxLength - 3);
        return max <= 0 ? "..." : message.substring(0, Math.min(max, message.length())) + "...";
    }

    private byte[] buildPostImage(RideDTO ride, String language, String rideLink) {
        BufferedImage background = null;
        try (InputStream inputStream = new ClassPathResource("static/img/facebook_fundal.png").getInputStream()) {
            background = ImageIO.read(inputStream);
        } catch (Exception ignored) {
            background = null;
        }

        int width = Math.max(800, imageWidth);
        int height = Math.max(420, imageHeight);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        if (background != null) {
            g.drawImage(background, 0, 0, width, height, null);
        } else {
            g.setColor(new Color(209, 250, 229));
            g.fillRect(0, 0, width, height);
        }

        g.setColor(new Color(236, 253, 245, 72));
        g.fillRect(0, 0, width, height);

        Color darkText = new Color(30, 48, 68);
        Color routeText = new Color(5, 82, 68);
        Color secondaryText = new Color(100, 116, 139);
        Color accent = new Color(16, 168, 108);
        Font regularFont = getRobotoRegular();
        Font boldFont = getRobotoBlack();
        boolean isRu = "ru".equals(language);
        boolean passengerRequest = isPassengerRequest(ride);

        String title = passengerRequest
            ? (isRu ? "Кто-нибудь едет?" : "Are cineva drum?")
            : (isRu ? "Предлагаю транспорт" : "Am drum");
        drawCenteredText(g, title, boldFont.deriveFont(Font.BOLD, height * 0.058f), darkText, width, (int) (height * 0.17));

        String fromMain = extractMainLocation(safe(ride.getFromLocation()));
        String toMain = extractMainLocation(safe(ride.getToLocation()));
        int[] routeCenters = drawRoute(g, fromMain, toMain, isRu, boldFont, regularFont, routeText,
            secondaryText, accent, width, height);

        String locationSubtitle = extractRestLocation(safe(ride.getToLocation()));
        int locationSubtitleCenter = routeCenters[1];
        if (locationSubtitle.isBlank()) {
            locationSubtitle = extractRestLocation(safe(ride.getFromLocation()));
            locationSubtitleCenter = routeCenters[0];
        }
        if (!locationSubtitle.isBlank()) {
            drawCenteredTextAt(g, locationSubtitle, regularFont.deriveFont(Font.PLAIN, height * 0.035f),
                secondaryText, locationSubtitleCenter, (int) (height * 0.39));
        }

        Locale locale = isRu ? Locale.forLanguageTag("ru-RU") : Locale.forLanguageTag("ro-RO");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", locale);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", locale);
        String travelDate = ride.getTravelDate() != null ? ride.getTravelDate().format(dateFormatter) : "-";
        String departureTime = Boolean.TRUE.equals(ride.getFlexibleTime())
            ? (isRu ? "Гибкое время" : "Oră flexibilă")
            : (ride.getDepartureTime() != null ? ride.getDepartureTime().format(timeFormatter) : "-");

        int cardWidth = (int) (width * 0.26);
        int cardHeight = (int) (height * 0.19);
        int cardGap = (int) (width * 0.025);
        int cardsTop = (int) (height * 0.49);
        int leftCardX = (width - (cardWidth * 2 + cardGap)) / 2;
        int rightCardX = leftCardX + cardWidth + cardGap;
        int bottomCardX = (width - cardWidth) / 2;
        int bottomCardY = cardsTop + cardHeight + (int) (height * 0.035);

        drawInfoCard(g, leftCardX, cardsTop, cardWidth, cardHeight, "calendar", travelDate,
            isRu ? "Дата поездки" : "Data călătoriei", boldFont, regularFont, darkText, secondaryText, accent);
        drawInfoCard(g, rightCardX, cardsTop, cardWidth, cardHeight, "clock", departureTime,
            Boolean.TRUE.equals(ride.getFlexibleTime())
                ? (isRu ? "Отправление на выбор" : "Plecare la alegere")
                : (isRu ? "Время отправления" : "Ora plecării"),
            boldFont, regularFont, darkText, secondaryText, accent);

        boolean packageOnly = Boolean.TRUE.equals(ride.getIsPackageOnly());
        boolean packagesIncluded = Boolean.TRUE.equals(ride.getTransportAndPackages());
        String countTitle;
        String countSubtitle;
        String countIcon = packageOnly ? "package" : "person";
        if (passengerRequest) {
            int count = ride.getRequestedSeats() != null ? ride.getRequestedSeats() : 1;
            countTitle = formatPassengerCount(count, isRu);
            countSubtitle = isRu ? "Ищет транспорт" : "Caută transport";
        } else if (packageOnly) {
            countTitle = isRu ? "Перевожу посылки" : "Transport colete";
            countSubtitle = isRu ? "Только посылки" : "Doar colete";
        } else {
            int count = ride.getAvailableSeats() != null ? ride.getAvailableSeats() : 0;
            countTitle = formatAvailableSeats(count, isRu);
            countSubtitle = packagesIncluded
                ? (isRu ? "Пассажиры и посылки" : "Pasageri și colete")
                : (isRu ? "Свободные места" : "Locuri disponibile");
        }
        drawInfoCard(g, bottomCardX, bottomCardY, cardWidth, cardHeight, countIcon, countTitle, countSubtitle,
            boldFont, regularFont, darkText, secondaryText, accent);

        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Nu am putut genera imaginea pentru postare.", e);
        }
    }

    private int[] drawRoute(Graphics2D g,
                           String from,
                           String to,
                           boolean isRu,
                           Font boldFont,
                           Font regularFont,
                           Color routeColor,
                           Color labelColor,
                           Color accentColor,
                           int width,
                           int height) {
        float fontSize = height * 0.085f;
        Font routeFont = boldFont.deriveFont(Font.BOLD, fontSize);
        FontMetrics metrics = g.getFontMetrics(routeFont);
        int arrowWidth = (int) (height * 0.065);
        int arrowGap = (int) (width * 0.022);
        int maxWidth = (int) (width * 0.84);
        while (fontSize > height * 0.052f
            && metrics.stringWidth(from) + metrics.stringWidth(to) + arrowWidth + arrowGap * 2 > maxWidth) {
            fontSize -= 2f;
            routeFont = boldFont.deriveFont(Font.BOLD, fontSize);
            metrics = g.getFontMetrics(routeFont);
        }

        int fromWidth = metrics.stringWidth(from);
        int toWidth = metrics.stringWidth(to);
        int totalWidth = fromWidth + toWidth + arrowWidth + arrowGap * 2;
        int x = (width - totalWidth) / 2;
        int fromCenter = x + fromWidth / 2;
        int baseline = (int) (height * 0.33);
        Font labelFont = regularFont.deriveFont(Font.BOLD, height * 0.024f);
        FontMetrics labelMetrics = g.getFontMetrics(labelFont);

        String fromLabel = isRu ? "ОТКУДА" : "DE LA";
        String toLabel = isRu ? "КУДА" : "PÂNĂ LA";
        g.setFont(labelFont);
        g.setColor(labelColor);
        g.drawString(fromLabel, x + (fromWidth - labelMetrics.stringWidth(fromLabel)) / 2,
            baseline - metrics.getAscent() - (int) (height * 0.012));

        g.setFont(routeFont);
        g.setColor(routeColor);
        g.drawString(from, x, baseline);
        x += fromWidth + arrowGap;

        g.setColor(accentColor);
        g.setStroke(new BasicStroke(Math.max(5f, height * 0.009f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int arrowY = baseline - metrics.getAscent() / 2;
        int arrowEndX = x + arrowWidth;
        g.drawLine(x, arrowY, arrowEndX, arrowY);
        g.drawLine(arrowEndX, arrowY, arrowEndX - arrowWidth / 3, arrowY - arrowWidth / 3);
        g.drawLine(arrowEndX, arrowY, arrowEndX - arrowWidth / 3, arrowY + arrowWidth / 3);
        x += arrowWidth + arrowGap;

        g.setFont(labelFont);
        g.setColor(labelColor);
        g.drawString(toLabel, x + (metrics.stringWidth(to) - labelMetrics.stringWidth(toLabel)) / 2,
            baseline - metrics.getAscent() - (int) (height * 0.012));
        g.setFont(routeFont);
        g.setColor(routeColor);
        g.drawString(to, x, baseline);
        return new int[]{fromCenter, x + toWidth / 2};
    }

    private void drawInfoCard(Graphics2D g,
                              int x,
                              int y,
                              int width,
                              int height,
                              String icon,
                              String title,
                              String subtitle,
                              Font boldFont,
                              Font regularFont,
                              Color titleColor,
                              Color subtitleColor,
                              Color accentColor) {
        int radius = Math.max(18, height / 6);
        g.setColor(new Color(15, 23, 42, 20));
        g.fillRoundRect(x + 3, y + 8, width, height, radius, radius);
        g.setColor(new Color(255, 255, 255, 238));
        g.fillRoundRect(x, y, width, height, radius, radius);

        int iconSize = (int) (height * 0.42);
        int iconX = x + (int) (width * 0.09);
        int iconY = y + (height - iconSize) / 2;
        drawCardIcon(g, icon, iconX, iconY, iconSize, accentColor);

        int textX = iconX + iconSize + (int) (width * 0.07);
        int maxTextWidth = x + width - textX - (int) (width * 0.06);
        Font titleFont = fitFont(g, boldFont, title, height * 0.22f, height * 0.15f, maxTextWidth);
        g.setFont(titleFont);
        g.setColor(titleColor);
        FontMetrics titleMetrics = g.getFontMetrics();
        int titleBaseline = y + (int) (height * 0.47);
        g.drawString(title, textX, titleBaseline);

        Font subtitleFont = fitFont(g, regularFont, subtitle, height * 0.17f, height * 0.12f, maxTextWidth);
        g.setFont(subtitleFont);
        g.setColor(subtitleColor);
        g.drawString(subtitle, textX, titleBaseline + Math.max(titleMetrics.getDescent() + 8, (int) (height * 0.25)));
    }

    private void drawCardIcon(Graphics2D g, String icon, int x, int y, int size, Color color) {
        g.setColor(color);
        g.setStroke(new BasicStroke(Math.max(3f, size * 0.07f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        if ("calendar".equals(icon)) {
            g.drawRoundRect(x + size / 10, y + size / 6, size * 4 / 5, size * 3 / 4, size / 8, size / 8);
            g.drawLine(x + size / 10, y + size * 2 / 5, x + size * 9 / 10, y + size * 2 / 5);
            g.drawLine(x + size / 3, y + size / 12, x + size / 3, y + size / 4);
            g.drawLine(x + size * 2 / 3, y + size / 12, x + size * 2 / 3, y + size / 4);
        } else if ("clock".equals(icon)) {
            g.drawOval(x + size / 12, y + size / 12, size * 5 / 6, size * 5 / 6);
            int centerX = x + size / 2;
            int centerY = y + size / 2;
            g.drawLine(centerX, centerY, centerX, y + size / 4);
            g.drawLine(centerX, centerY, x + size * 2 / 3, y + size * 3 / 5);
        } else if ("package".equals(icon)) {
            g.drawRoundRect(x + size / 10, y + size / 5, size * 4 / 5, size * 2 / 3, size / 12, size / 12);
            g.drawLine(x + size / 10, y + size / 3, x + size * 9 / 10, y + size / 3);
            g.drawLine(x + size / 2, y + size / 5, x + size / 2, y + size * 5 / 6);
        } else {
            g.drawOval(x + size * 3 / 8, y + size / 12, size / 4, size / 4);
            g.fillRoundRect(x + size / 5, y + size * 2 / 5, size * 3 / 5, size / 2, size / 3, size / 3);
        }
    }

    private Font fitFont(Graphics2D g, Font baseFont, String text, float preferredSize, float minimumSize, int maxWidth) {
        float size = preferredSize;
        Font font = baseFont.deriveFont(Font.PLAIN, size);
        while (size > minimumSize && g.getFontMetrics(font).stringWidth(text) > maxWidth) {
            size -= 1f;
            font = baseFont.deriveFont(Font.PLAIN, size);
        }
        return font;
    }

    private void drawCenteredText(Graphics2D g, String text, Font font, Color color, int width, int baseline) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, (width - metrics.stringWidth(text)) / 2, baseline);
    }

    private void drawCenteredTextAt(Graphics2D g, String text, Font font, Color color, int centerX, int baseline) {
        g.setFont(font);
        g.setColor(color);
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, centerX - metrics.stringWidth(text) / 2, baseline);
    }

    private String formatPassengerCount(int count, boolean isRu) {
        if (isRu) {
            return count + (count == 1 ? " пассажир" : " пассажира");
        }
        return count + (count == 1 ? " pasager" : " pasageri");
    }

    private String formatAvailableSeats(int count, boolean isRu) {
        if (isRu) {
            return count + (count == 1 ? " место" : " места");
        }
        return count + (count == 1 ? " loc disponibil" : " locuri disponibile");
    }

    private int drawLine(Graphics2D g, String text, int x, int y) {
        if (text == null) {
            return y;
        }
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, x, y);
        return y + metrics.getHeight();
    }

    private boolean isPassengerRequest(RideDTO ride) {
        return ride != null && ride.getAnnouncementType() == AnnouncementType.PASSENGER_REQUEST;
    }

    private int drawCenteredLine(Graphics2D g, String text, int padding, int width, int y) {
        if (text == null) {
            return y;
        }
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int x = Math.max(padding, (width - textWidth) / 2);
        g.drawString(text, x, y);
        return y + metrics.getHeight();
    }

    private int drawCenteredLineTop(Graphics2D g, String text, int padding, int width, int top) {
        if (text == null) {
            return top;
        }
        FontMetrics metrics = g.getFontMetrics();
        int textWidth = metrics.stringWidth(text);
        int x = Math.max(padding, (width - textWidth) / 2);
        int baseline = top + metrics.getAscent();
        g.drawString(text, x, baseline);
        return top + metrics.getHeight();
    }

    private java.util.List<String> wrapText(String text, FontMetrics metrics, int maxWidth, int maxLines) {
        java.util.List<String> lines = new java.util.ArrayList<>();
        if (text == null || text.isBlank()) {
            return lines;
        }

        String[] words = text.trim().split("\\s+");
        StringBuilder current = new StringBuilder();
        int index = 0;

        while (index < words.length && lines.size() < maxLines) {
            String word = words[index];
            String candidate = current.isEmpty() ? word : current + " " + word;
            if (metrics.stringWidth(candidate) <= maxWidth) {
                current.setLength(0);
                current.append(candidate);
                index++;
                continue;
            }

            if (!current.isEmpty()) {
                lines.add(current.toString());
                current.setLength(0);
                continue;
            }

            lines.add(truncateToWidth(word, metrics, maxWidth));
            index++;
        }

        if (lines.size() < maxLines && current.length() > 0) {
            lines.add(current.toString());
        } else if (index < words.length && !lines.isEmpty()) {
            int lastIndex = lines.size() - 1;
            lines.set(lastIndex, appendEllipsis(lines.get(lastIndex), metrics, maxWidth));
        }

        return lines;
    }

    private String extractMainLocation(String location) {
        if (location == null) {
            return "";
        }
        int commaIndex = location.indexOf(',');
        if (commaIndex <= 0) {
            return location.trim();
        }
        return location.substring(0, commaIndex).trim();
    }

    private String extractRestLocation(String location) {
        if (location == null) {
            return "";
        }
        int commaIndex = location.indexOf(',');
        if (commaIndex < 0 || commaIndex >= location.length() - 1) {
            return "";
        }
        return location.substring(commaIndex + 1).trim();
    }

    private String buildRestRoute(String fromRest, String toRest) {
        String left = fromRest == null ? "" : fromRest.trim();
        String right = toRest == null ? "" : toRest.trim();
        if (left.isBlank() && right.isBlank()) {
            return "";
        }
        if (left.isBlank()) {
            return right;
        }
        if (right.isBlank()) {
            return left;
        }
        return left + "   " + right;
    }

    private String buildMixedRouteLine(String fromMain, String fromRest, String toMain, String toRest) {
        String leftMain = fromMain == null ? "" : fromMain.trim();
        String rightMain = toMain == null ? "" : toMain.trim();
        String leftRest = fromRest == null ? "" : fromRest.trim();
        String rightRest = toRest == null ? "" : toRest.trim();

        String left = leftMain.isBlank() ? "" : leftMain;
        String right = rightMain.isBlank() ? "" : rightMain;

        if (!leftRest.isBlank()) {
            left = left + " (" + leftRest + ")";
        }
        if (!rightRest.isBlank()) {
            right = right + " (" + rightRest + ")";
        }

        if (left.isBlank() && right.isBlank()) {
            return "";
        }
        if (left.isBlank()) {
            return right;
        }
        if (right.isBlank()) {
            return left;
        }
        return left + " -> " + right;
    }

    private int drawRouteLineMixed(Graphics2D g,
                                   int padding,
                                   int width,
                                   int y,
                                   int maxWidth,
                                   String fromMain,
                                   String fromRest,
                                   String toMain,
                                   String toRest,
                                   Font mainFont,
                                   Font restFont,
                                   Color mainColor,
                                   Color restColor,
                                   Color arrowColor) {
        String leftMain = fromMain == null ? "" : fromMain.trim();
        String rightMain = toMain == null ? "" : toMain.trim();
        String leftRest = fromRest == null ? "" : fromRest.trim();
        String rightRest = toRest == null ? "" : toRest.trim();

        String leftText = leftRest.isBlank() ? leftMain : leftMain + " (" + leftRest + ")";
        String rightText = rightRest.isBlank() ? rightMain : rightMain + " (" + rightRest + ")";

        String fullText = leftText;
        if (!leftText.isBlank() && !rightText.isBlank()) {
            fullText = leftText + " -> " + rightText;
        } else if (!rightText.isBlank()) {
            fullText = rightText;
        }

        FontMetrics mainMetrics = g.getFontMetrics(mainFont);
        FontMetrics restMetrics = g.getFontMetrics(restFont);
        int textWidth = measureRouteWidth(mainMetrics, restMetrics, leftMain, leftRest, rightMain, rightRest);

        int x = Math.max(padding, (width - textWidth) / 2);
        int baseline = y + (int) (g.getFontMetrics(mainFont).getAscent());

        int currentX = x;
        if (!leftMain.isBlank()) {
            g.setFont(mainFont);
            g.setColor(mainColor);
            g.drawString(leftMain, currentX, baseline);
            currentX += mainMetrics.stringWidth(leftMain);
        }
        if (!leftRest.isBlank()) {
            String restPart = " (" + leftRest + ")";
            g.setFont(restFont);
            g.setColor(restColor);
            g.drawString(restPart, currentX, baseline);
            currentX += restMetrics.stringWidth(restPart);
        }

        if (!leftText.isBlank() && !rightText.isBlank()) {
            String arrow = " -> ";
            g.setFont(mainFont);
            g.setColor(arrowColor);
            g.drawString(arrow, currentX, baseline);
            currentX += mainMetrics.stringWidth(arrow);
        }

        if (!rightMain.isBlank()) {
            g.setFont(mainFont);
            g.setColor(mainColor);
            g.drawString(rightMain, currentX, baseline);
            currentX += mainMetrics.stringWidth(rightMain);
        }
        if (!rightRest.isBlank()) {
            String restPart = " (" + rightRest + ")";
            g.setFont(restFont);
            g.setColor(restColor);
            g.drawString(restPart, currentX, baseline);
        }

        int lineHeight = Math.max(mainMetrics.getHeight(), restMetrics.getHeight());
        return y + lineHeight;
    }

    private int drawLabeledLocationLine(Graphics2D g,
                                        int padding,
                                        int width,
                                        int y,
                                        String label,
                                        String mainLocation,
                                        String restLocation,
                                        Font labelFont,
                                        Font mainFont,
                                        Font restFont,
                                        Color labelColor,
                                        Color mainColor,
                                        Color restColor) {
        String safeLabel = safe(label);
        String safeMain = safe(mainLocation);
        String safeRest = safe(restLocation);
        FontMetrics labelMetrics = g.getFontMetrics(labelFont);
        FontMetrics mainMetrics = g.getFontMetrics(mainFont);
        FontMetrics restMetrics = g.getFontMetrics(restFont);
        String labelPart = safeLabel + " ";
        String restPart = safeRest.isBlank() ? "" : " (" + safeRest + ")";
        int textWidth = labelMetrics.stringWidth(labelPart)
            + mainMetrics.stringWidth(safeMain)
            + restMetrics.stringWidth(restPart);
        int currentX = Math.max(padding, (width - textWidth) / 2);
        int ascent = Math.max(labelMetrics.getAscent(), Math.max(mainMetrics.getAscent(), restMetrics.getAscent()));
        int baseline = y + ascent;

        g.setFont(labelFont);
        g.setColor(labelColor);
        g.drawString(labelPart, currentX, baseline);
        currentX += labelMetrics.stringWidth(labelPart);

        g.setFont(mainFont);
        g.setColor(mainColor);
        g.drawString(safeMain, currentX, baseline);
        currentX += mainMetrics.stringWidth(safeMain);

        if (!restPart.isBlank()) {
            g.setFont(restFont);
            g.setColor(restColor);
            g.drawString(restPart, currentX, baseline);
        }

        return y + Math.max(labelMetrics.getHeight(), Math.max(mainMetrics.getHeight(), restMetrics.getHeight()));
    }

    private int measureRouteWidth(FontMetrics mainMetrics,
                                  FontMetrics restMetrics,
                                  String fromMain,
                                  String fromRest,
                                  String toMain,
                                  String toRest) {
        int width = 0;
        if (fromMain != null && !fromMain.isBlank()) {
            width += mainMetrics.stringWidth(fromMain);
        }
        if (fromRest != null && !fromRest.isBlank()) {
            width += restMetrics.stringWidth(" (" + fromRest + ")");
        }
        if ((fromMain != null && !fromMain.isBlank()) && (toMain != null && !toMain.isBlank())) {
            width += mainMetrics.stringWidth(" -> ");
        }
        if (toMain != null && !toMain.isBlank()) {
            width += mainMetrics.stringWidth(toMain);
        }
        if (toRest != null && !toRest.isBlank()) {
            width += restMetrics.stringWidth(" (" + toRest + ")");
        }
        return width;
    }

    private int drawDateTimeLine(Graphics2D g,
                                 int padding,
                                 int width,
                                 int y,
                                 String dateLabel,
                                 String dateValue,
                                 String timeLabel,
                                 String timeValue,
                                 Font labelFont,
                                 Font valueFont,
                                 Color labelColor,
                                 Color valueColor) {
        String leftLabel = dateLabel + ": ";
        String rightLabel = timeLabel + ": ";
        String separator = "   |   ";

        FontMetrics labelMetrics = g.getFontMetrics(labelFont);
        FontMetrics valueMetrics = g.getFontMetrics(valueFont);

        int totalWidth = 0;
        totalWidth += labelMetrics.stringWidth(leftLabel);
        totalWidth += valueMetrics.stringWidth(dateValue);
        totalWidth += labelMetrics.stringWidth(separator);
        totalWidth += labelMetrics.stringWidth(rightLabel);
        totalWidth += valueMetrics.stringWidth(timeValue);

        int x = Math.max(padding, (width - totalWidth) / 2);
        int baseline = y + Math.max(labelMetrics.getAscent(), valueMetrics.getAscent());

        g.setFont(labelFont);
        g.setColor(labelColor);
        g.drawString(leftLabel, x, baseline);
        x += labelMetrics.stringWidth(leftLabel);

        g.setFont(valueFont);
        g.setColor(valueColor);
        g.drawString(dateValue, x, baseline);
        x += valueMetrics.stringWidth(dateValue);

        g.setFont(labelFont);
        g.setColor(labelColor);
        g.drawString(separator, x, baseline);
        x += labelMetrics.stringWidth(separator);

        g.drawString(rightLabel, x, baseline);
        x += labelMetrics.stringWidth(rightLabel);

        g.setFont(valueFont);
        g.setColor(valueColor);
        g.drawString(timeValue, x, baseline);

        int lineHeight = Math.max(labelMetrics.getHeight(), valueMetrics.getHeight());
        return y + lineHeight;
    }

    private Font getRobotoRegular() {
        if (robotoRegular != null) {
            return robotoRegular;
        }
        robotoRegular = loadFontFromResource("fonts/Roboto-Regular.ttf", Font.PLAIN);
        return robotoRegular;
    }

    private Font getRobotoBlack() {
        if (robotoBlack != null) {
            return robotoBlack;
        }
        robotoBlack = loadFontFromResource("fonts/Roboto-Black.ttf", Font.BOLD);
        return robotoBlack;
    }

    private Font loadFontFromResource(String path, int style) {
        try (InputStream inputStream = new ClassPathResource(path).getInputStream()) {
            Font font = Font.createFont(Font.TRUETYPE_FONT, inputStream);
            return font.deriveFont(style, 12f);
        } catch (Exception e) {
            return new Font("SansSerif", style, 12);
        }
    }

    private String truncateToWidth(String text, FontMetrics metrics, int maxWidth) {
        String ellipsis = "...";
        if (metrics.stringWidth(text) <= maxWidth) {
            return text;
        }
        int max = Math.max(0, text.length() - 1);
        while (max > 0 && metrics.stringWidth(text.substring(0, max) + ellipsis) > maxWidth) {
            max--;
        }
        return max > 0 ? text.substring(0, max) + ellipsis : ellipsis;
    }

    private String appendEllipsis(String text, FontMetrics metrics, int maxWidth) {
        String ellipsis = "...";
        if (metrics.stringWidth(text + ellipsis) <= maxWidth) {
            return text + ellipsis;
        }
        return truncateToWidth(text, metrics, maxWidth);
    }

    private String resolveBaseUrl(HttpServletRequest request) {
        if (baseUrlConfig != null && !baseUrlConfig.trim().isEmpty()) {
            return trimTrailingSlash(baseUrlConfig.trim());
        }
        return trimTrailingSlash(buildBaseUrl(request));
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
                if (("http".equalsIgnoreCase(proto) && port != 80)
                    || ("https".equalsIgnoreCase(proto) && port != 443)) {
                    portPart = ":" + port;
                }
            }
        }

        return proto + "://" + host + portPart;
    }

    private String trimTrailingSlash(String value) {
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String optionalHeader(HttpServletRequest request, String headerName) {
        String value = request.getHeader(headerName);
        return value == null ? "" : value.trim();
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }

    private String safeNumber(Integer value) {
        return value == null ? "-" : value.toString();
    }
}
