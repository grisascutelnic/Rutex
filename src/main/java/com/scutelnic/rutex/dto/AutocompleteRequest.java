package com.scutelnic.rutex.dto;

public class AutocompleteRequest {
    private String query;
    private String language = "ro";
    private Integer limit = 10;
    private Double latitude;
    private Double longitude;
    private Integer radius = 50000; // 50km default radius for Moldova
    
    public AutocompleteRequest() {}
    
    public AutocompleteRequest(String query) {
        this.query = query;
    }
    
    public AutocompleteRequest(String query, String language) {
        this.query = query;
        this.language = language;
    }
    
    // Getters and Setters
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public String getLanguage() {
        return language;
    }
    
    public void setLanguage(String language) {
        this.language = language;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public Double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }
    
    public Double getLongitude() {
        return longitude;
    }
    
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
    
    public Integer getRadius() {
        return radius;
    }
    
    public void setRadius(Integer radius) {
        this.radius = radius;
    }
}
