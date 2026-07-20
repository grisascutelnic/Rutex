package com.scutelnic.rutex.dto;

public record RouteSeoContentUpdateRequest(
        String routeDescription,
        String fromDescription,
        String toDescription,
        String nearbyDirectionsText,
        String frequentSearchesText
) {
}
