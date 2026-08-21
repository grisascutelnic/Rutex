package com.scutelnic.rutex.service;

import com.scutelnic.rutex.dto.RideDTO;
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

    @Value("${facebook.image.width:1200}")
    private int imageWidth;

    @Value("${facebook.image.height:630}")
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
        String transportLine;
        if (isRu) {
            transportLine = isPackageOnly ? "только посылки" : "пассажиры";
            if (!isPackageOnly && transportAndPackages) {
                transportLine = "пассажиры + посылки";
            }
        } else {
            transportLine = isPackageOnly ? "doar colete" : "pasageri";
            if (!isPackageOnly && transportAndPackages) {
                transportLine = "pasageri + colete";
            }
        }

        String description = safe(ride.getDescription());

        StringBuilder message = new StringBuilder();
        if (isRu) {
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
        if (isRu) {
            message.append("🚚 Транспорт ").append(transportLine);
        } else {
            message.append("🚚 Transport ").append(transportLine);
        }

        if (!description.isBlank()) {
            message.append(System.lineSeparator()).append("📝 ").append(description);
        }

        message.append(System.lineSeparator())
            .append("👇 Pentru a contacta șoferul apasă aici")
            .append(System.lineSeparator())
            .append("🔗 ").append(rideLink);
        return message.toString();
    }

    private String applyTextFormatIfEnabled(String message) {
        if (!textFormatEnabled) {
            return message;
        }
        if (textFormatMaxLength <= 0) {
            return message;
        }
        if (message == null || message.length() <= textFormatMaxLength) {
            return message;
        }
        int max = Math.max(0, textFormatMaxLength - 3);
        if (max <= 0) {
            return "...";
        }
        return message.substring(0, Math.min(max, message.length())) + "...";
    }

    private byte[] buildPostImage(RideDTO ride, String language, String rideLink) {
        int width = Math.max(800, imageWidth);
        int height = Math.max(420, imageHeight);

        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        boolean backgroundLoaded = false;
        try (InputStream inputStream = new ClassPathResource("static/img/facebook_fundal.png").getInputStream()) {
            BufferedImage background = ImageIO.read(inputStream);
            if (background != null) {
                g.drawImage(background, 0, 0, width, height, null);
                backgroundLoaded = true;
            }
        } catch (Exception ignored) {
            backgroundLoaded = false;
        }

        if (!backgroundLoaded) {
            g.setColor(new Color(209, 250, 229));
            g.fillRect(0, 0, width, height);
        }

        int padding = (int) (width * 0.06);

        Color primaryText = new Color(70, 70, 70);
        Color secondaryText = new Color(70, 70, 70);
        Color lightText = new Color(70, 70, 70);
        Color accentText = primaryText;
        g.setColor(lightText);
        Font baseFont = getRobotoRegular();
        Font boldFont = getRobotoBlack();
        Font titleFont = boldFont.deriveFont(Font.BOLD, (float) (height * 0.105));
        Font routeFont = boldFont.deriveFont(Font.BOLD, (float) (height * 0.095));
        float seatsSize = (float) (height * 0.058);
        Font seatsFont = baseFont.deriveFont(Font.PLAIN, seatsSize);
        Font detailFont = seatsFont;
        Font dateValueFont = boldFont.deriveFont(Font.BOLD, seatsSize);
        Font subDetailFont = baseFont.deriveFont(Font.PLAIN, (float) (height * 0.045));
        Font descriptionFont = baseFont.deriveFont(Font.PLAIN, (float) (height * 0.065));

        String routeTitle = "ru".equals(language) ? "Еду:" : "Am drum:";
        FontMetrics titleMetrics = g.getFontMetrics(titleFont);
        FontMetrics routeMetrics = g.getFontMetrics(routeFont);
        FontMetrics restMetrics = g.getFontMetrics(subDetailFont);
        FontMetrics dateLabelMetrics = g.getFontMetrics(detailFont);
        FontMetrics dateValueMetrics = g.getFontMetrics(dateValueFont);
        FontMetrics seatsMetrics = g.getFontMetrics(seatsFont);
        FontMetrics descMetrics = g.getFontMetrics(descriptionFont);

        int titleHeight = titleMetrics.getHeight();
        int routeHeight = Math.max(routeMetrics.getHeight(), restMetrics.getHeight());
        int dateHeight = Math.max(dateLabelMetrics.getHeight(), dateValueMetrics.getHeight());
        int seatsHeight = seatsMetrics.getHeight();
        int descHeight = descMetrics.getHeight();

        boolean hasDescription = !safe(ride.getDescription()).isBlank();
        int rows = hasDescription ? 5 : 4;
        int gap = (int) (height * 0.035);
        int totalHeight = titleHeight + routeHeight + dateHeight + seatsHeight + (hasDescription ? descHeight : 0)
            + gap * (rows - 1);
        int top = Math.max(0, (height - totalHeight) / 2);

        int cursorY = top;
        g.setFont(titleFont);
        cursorY = drawCenteredLineTop(g, routeTitle, padding, width, cursorY);
        cursorY += gap;

        String fromLocation = safe(ride.getFromLocation());
        String toLocation = safe(ride.getToLocation());
        String fromMain = extractMainLocation(fromLocation);
        String fromRest = extractRestLocation(fromLocation);
        String toMain = extractMainLocation(toLocation);
        String toRest = extractRestLocation(toLocation);

        int routeMaxWidth = width - (padding * 2);
        cursorY = drawRouteLineMixed(g, padding, width, cursorY, routeMaxWidth,
            fromMain, fromRest, toMain, toRest, routeFont, subDetailFont, primaryText, secondaryText, accentText);
        cursorY += gap;

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", new Locale("ro", "RO"));
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm", new Locale("ro", "RO"));
        String travelDate = ride.getTravelDate() != null ? ride.getTravelDate().format(dateFormatter) : "-";
        String departureTime = Boolean.TRUE.equals(ride.getFlexibleTime())
            ? ("ru".equals(language) ? "Гибкое время" : "Oră flexibilă")
            : (ride.getDepartureTime() != null ? ride.getDepartureTime().format(timeFormatter) : "-");

        String dateLabel = "ru".equals(language) ? "Дата" : "Data";
        String timeLabel = "ru".equals(language) ? "Время" : "Ora";
        String line1 = String.format("%s: %s   |   %s: %s", dateLabel, travelDate, timeLabel, departureTime);

        boolean isPackageOnly = Boolean.TRUE.equals(ride.getIsPackageOnly());
        boolean transportAndPackages = Boolean.TRUE.equals(ride.getTransportAndPackages());
        String seatsLabel;
        if ("ru".equals(language)) {
            seatsLabel = isPackageOnly ? "Только посылки" : "Мест: " + safeNumber(ride.getAvailableSeats());
            if (!isPackageOnly && transportAndPackages) {
                seatsLabel += " (пассажиры + посылки)";
            }
        } else {
            seatsLabel = isPackageOnly ? "Transport doar colete" : "Locuri: " + safeNumber(ride.getAvailableSeats());
            if (!isPackageOnly && transportAndPackages) {
                seatsLabel += " (pasageri + colete)";
            }
        }

        cursorY = drawDateTimeLine(g, padding, width, cursorY,
            dateLabel, travelDate, timeLabel, departureTime, detailFont, dateValueFont, lightText, primaryText);
        cursorY += gap;
        g.setFont(seatsFont);
        cursorY = drawCenteredLineTop(g, seatsLabel, padding, width, cursorY);

        String description = safe(ride.getDescription());
        if (!description.isBlank()) {
            cursorY += gap;
            g.setColor(lightText);
            g.setFont(descriptionFont);
            int maxWidth = width - (padding * 2);
            for (String line : wrapText(description, g.getFontMetrics(), maxWidth, 2)) {
                cursorY = drawCenteredLineTop(g, line, padding, width, cursorY);
                cursorY += (int) (height * 0.01);
            }
        }

        g.dispose();

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Nu am putut genera imaginea pentru postare.", e);
        }
    }

    private int drawLine(Graphics2D g, String text, int x, int y) {
        if (text == null) {
            return y;
        }
        FontMetrics metrics = g.getFontMetrics();
        g.drawString(text, x, y);
        return y + metrics.getHeight();
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
