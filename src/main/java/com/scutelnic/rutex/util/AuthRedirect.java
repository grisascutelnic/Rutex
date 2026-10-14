package com.scutelnic.rutex.util;

import java.net.URI;

public final class AuthRedirect {
    public static final String SESSION_ATTRIBUTE = "authRedirect";

    private AuthRedirect() {
    }

    public static String safeTarget(String target) {
        if (target == null || !target.startsWith("/") || target.startsWith("//") || target.contains("\\")
                || target.chars().anyMatch(character -> character < 32)) {
            return null;
        }
        try {
            URI uri = URI.create(target).normalize();
            String path = uri.getPath();
            if (uri.getRawAuthority() != null || path == null || path.startsWith("//") || path.contains("\\")
                    || path.chars().anyMatch(character -> character < 32)
                    || path.matches(".*/(login|register|logout|forgot-password|reset-password)(/.*)?")
                    || path.startsWith("/auth/") || path.startsWith("/oauth2/")) {
                return null;
            }
            return uri.toString();
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }
}
