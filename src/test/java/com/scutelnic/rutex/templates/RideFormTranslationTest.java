package com.scutelnic.rutex.templates;

import org.junit.jupiter.api.Test;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.StringTemplateResolver;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class RideFormTranslationTest {
    @Test
    void travelNoticeAndInformButtonAreVisibleOnlyToOtherVisitors() throws Exception {
        var engine = new SpringTemplateEngine();
        engine.setTemplateResolver(new StringTemplateResolver());
        String source = Files.readString(Path.of("src/main/resources/templates/ride-details.html"));
        int noticeStart = source.indexOf("<div class=\"ride-reservation-note\"");
        String notice = source.substring(noticeStart, source.indexOf("</div>", noticeStart) + 6);
        int buttonStart = source.indexOf("<button class=\"btn-contact-driver btn-reserve-ride\"");
        String button = source.substring(buttonStart, source.indexOf("</button>", buttonStart) + 9);
        var context = new Context();
        context.setVariable("ride", Map.of("userId", 7L));
        context.setVariable("translations", Map.of());

        for (Long userId : new Long[]{null, 7L, 9L}) {
            context.setVariable("currentUser", userId == null ? null : Map.of("id", userId));
            String rendered = engine.process(notice + button, context);
            boolean shouldShow = userId == null || userId != 7L;
            assertEquals(shouldShow, rendered.contains("ride-reservation-note"));
            assertEquals(shouldShow, rendered.contains("open-reservation-modal"));
        }
    }

    @Test
    void bothFormsRenderRussianEvenWithoutDatabaseTranslations() throws Exception {
        var engine = new SpringTemplateEngine();
        var fragments = new ClassLoaderTemplateResolver();
        fragments.setPrefix("templates/");
        fragments.setSuffix(".html");
        fragments.setResolvablePatterns(Set.of("fragments/*"));
        fragments.setOrder(1);
        fragments.setCharacterEncoding("UTF-8");
        engine.addTemplateResolver(fragments);
        var strings = new StringTemplateResolver();
        strings.setOrder(2);
        engine.addTemplateResolver(strings);
        for (String page : new String[]{"add-ride", "edit-ride"}) {
            String source = Files.readString(Path.of("src/main/resources/templates/fragments/ride-form.html"));
            String form = source.substring(source.indexOf("<form"), source.indexOf("</form>") + 7);
            // URL resolution requires a servlet context; this test checks form text and attributes.
            form = form.replaceAll("th:href=\"[^\"]*\"", "");
            var context = new Context();
            context.setVariable("currentLanguage", "ru");
            context.setVariable("editing", page.equals("edit-ride"));
            context.setVariable("translations", Map.of());
            String rendered = engine.process(form, context);
            assertTrue(rendered.contains("Я согласен показывать мой номер телефона"));
            assertTrue(rendered.contains("id=\"contact-phone\""));
            assertTrue(rendered.contains("id=\"" + page + "-form\""));
            assertTrue(rendered.contains("departure-hour"));
            assertTrue(rendered.contains("ride-step-tabs"));
            assertTrue(rendered.contains("Только пассажиры"));
            assertFalse(rendered.contains("id=\"transport-and-packages\""));
            assertTrue(rendered.contains("Условиями использования"));
            assertTrue(rendered.contains("Описание (необязательно)"));
            assertTrue(rendered.contains("Тип перевозки"));
            assertFalse(rendered.contains("Sunt de acord"));
            assertFalse(rendered.contains("Transport Pasageri"));
            assertFalse(rendered.contains("Introduceți"));
            if (page.equals("add-ride")) {
                assertTrue(rendered.contains("Что вы хотите опубликовать?"));
                assertTrue(rendered.contains("Ищу поездку"));
                assertTrue(rendered.contains("Время отправления гибкое"));
            }
            context.setVariable("currentLanguage", "ro");
            String romanian = engine.process(form, context);
            assertTrue(romanian.contains("Sunt de acord ca numărul meu de telefon"));
            assertTrue(romanian.contains("Termenii și Condițiile"));
            assertFalse(romanian.contains("Я согласен"));
        }
    }

    @Test
    void announcementWithoutPhoneHasOnlyTheMessageAction() throws Exception {
        var engine = new SpringTemplateEngine();
        var fragments = new ClassLoaderTemplateResolver();
        fragments.setPrefix("templates/");
        fragments.setSuffix(".html");
        fragments.setResolvablePatterns(Set.of("fragments/*"));
        fragments.setOrder(1);
        fragments.setCharacterEncoding("UTF-8");
        engine.addTemplateResolver(fragments);
        var strings = new StringTemplateResolver();
        strings.setOrder(2);
        engine.addTemplateResolver(strings);
        String source = Files.readString(Path.of("src/main/resources/templates/ride-details.html"));
        int start = source.indexOf("<div class=\"driver-contact\">");
        int messageButton = source.indexOf("id=\"ride-message-btn\"", start);
        String contact = source.substring(start, source.indexOf("</button>", messageButton) + 9) + "</div></div>";
        var context = new Context();
        context.setVariable("currentLanguage", "ro");
        context.setVariable("driver", Map.of("id", 7L));
        String hidden = engine.process(contact, context);
        assertTrue(hidden.contains("Trimite mesaj"));
        assertFalse(hidden.contains("driver-phone-value"));
        assertFalse(hidden.contains("toggle-driver-contact-btn"));
        assertFalse(hidden.contains("driver-email"));
        assertFalse(hidden.contains("Nu este specificat"));
        context.setVariable("driverMaskedPhone", "+373 69***456");
        String visible = engine.process(contact, context);
        assertTrue(visible.contains("driver-phone-value"));
        assertTrue(visible.contains("toggle-driver-contact-btn"));
        assertFalse(visible.contains("driver-email"));
    }
}
