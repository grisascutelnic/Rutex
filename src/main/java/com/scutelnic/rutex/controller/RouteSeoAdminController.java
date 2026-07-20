package com.scutelnic.rutex.controller;

import com.scutelnic.rutex.dto.RouteSeoContentUpdateRequest;
import com.scutelnic.rutex.dto.RouteMoveRequest;
import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.service.RouteSeoContentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/route-seo")
public class RouteSeoAdminController {

    private final RouteSeoContentService routeSeoContentService;

    public RouteSeoAdminController(RouteSeoContentService routeSeoContentService) {
        this.routeSeoContentService = routeSeoContentService;
    }

    @PostMapping("/{language}/{routeSlug}/regenerate")
    public ResponseEntity<Map<String, Object>> regenerate(@PathVariable String language,
                                                          @PathVariable String routeSlug,
                                                          HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Trebuie să fiți autentificat."));
        }
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Doar administratorii pot regenera conținutul."));
        }
        if (!"ro".equals(language) && !"ru".equals(language)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Limba nu este validă."));
        }

        try {
            routeSeoContentService.regenerate(routeSlug, language);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Conținutul AI a fost actualizat cu succes."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PutMapping("/{language}/{routeSlug}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable String language,
                                                      @PathVariable String routeSlug,
                                                      @RequestBody RouteSeoContentUpdateRequest request,
                                                      HttpSession session) {
        ResponseEntity<Map<String, Object>> authorizationError = validateAdmin(language, session);
        if (authorizationError != null) {
            return authorizationError;
        }

        try {
            routeSeoContentService.updateManually(routeSlug, language, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Conținutul rutei a fost salvat."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Salvarea conținutului a eșuat."));
        }
    }

    @PutMapping("/{language}/{routeSlug}/move")
    public ResponseEntity<Map<String, Object>> move(@PathVariable String language,
                                                    @PathVariable String routeSlug,
                                                    @RequestBody RouteMoveRequest request,
                                                    HttpSession session) {
        ResponseEntity<Map<String, Object>> authorizationError = validateAdmin(language, session);
        if (authorizationError != null) {
            return authorizationError;
        }

        try {
            String newSlug = routeSeoContentService.moveRoute(routeSlug, request);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Traseul și URL-ul au fost actualizate.",
                    "newSlug", newSlug
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Mutarea traseului a eșuat."));
        }
    }

    @DeleteMapping("/{language}/{routeSlug}")
    public ResponseEntity<Map<String, Object>> hide(@PathVariable String language,
                                                    @PathVariable String routeSlug,
                                                    HttpSession session) {
        ResponseEntity<Map<String, Object>> authorizationError = validateAdmin(language, session);
        if (authorizationError != null) {
            return authorizationError;
        }

        try {
            routeSeoContentService.hideRoute(routeSlug);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Ruta a fost eliminată din catalog și sitemap."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Ștergerea rutei a eșuat."));
        }
    }

    private ResponseEntity<Map<String, Object>> validateAdmin(String language, HttpSession session) {
        User currentUser = (User) session.getAttribute("user");
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("success", false, "message", "Trebuie să fiți autentificat."));
        }
        boolean isAdmin = currentUser.getRoles() != null && currentUser.getRoles().stream()
                .anyMatch(role -> "ROLE_ADMIN".equals(role.getName()));
        if (!isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("success", false, "message", "Doar administratorii pot edita conținutul."));
        }
        if (!"ro".equals(language) && !"ru".equals(language)) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Limba nu este validă."));
        }
        return null;
    }
}
