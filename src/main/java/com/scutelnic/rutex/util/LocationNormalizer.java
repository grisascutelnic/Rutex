package com.scutelnic.rutex.util;

import java.text.Normalizer;

public final class LocationNormalizer {

    private LocationNormalizer() {
    }

    public static String normalizeIfRedundant(String location) {
        if (location == null) {
            return null;
        }
        String trimmed = location.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }

        int commaIndex = trimmed.indexOf(',');
        if (commaIndex < 0) {
            return trimmed;
        }

        String left = trimmed.substring(0, commaIndex).trim();
        String right = trimmed.substring(commaIndex + 1).trim();
        if (left.isEmpty() || right.isEmpty()) {
            return trimmed;
        }

        String normalizedLeft = normalizeForCompare(left);
        String normalizedRight = normalizeForCompare(right);
        if (normalizedRight.contains(normalizedLeft)) {
            return left;
        }

        return trimmed;
    }

    private static String normalizeForCompare(String value) {
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase();
        return normalized.replaceAll("\\s+", " ").trim();
    }
}
