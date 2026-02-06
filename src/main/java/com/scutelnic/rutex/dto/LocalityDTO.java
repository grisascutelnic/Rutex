package com.scutelnic.rutex.dto;

import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.util.LocationNormalizer;

public class LocalityDTO {
    private Long id;
    private String nameRo;
    private String nameRu;
    private String googlePlaceId;
    private Double latitude;
    private Double longitude;
    private String type;
    private String districtNameRo;
    private String districtNameRu;
    private String countryCode;
    private String countryNameRo;
    private String countryNameRu;
    private Integer searchCount;
    
    public LocalityDTO() {}
    
    public LocalityDTO(Locality locality) {
        this.id = locality.getId();
        this.nameRo = normalizeLocation(locality.getNameRo());
        this.nameRu = normalizeLocation(locality.getNameRu());
        this.googlePlaceId = locality.getGooglePlaceId();
        this.latitude = locality.getLatitude();
        this.longitude = locality.getLongitude();
        this.type = locality.getType() != null ? locality.getType().getValue() : null;
        this.searchCount = locality.getSearchCount();
        
        if (locality.getDistrict() != null) {
            String rawDistrictRo = locality.getDistrict().getNameRo();
            String rawDistrictRu = locality.getDistrict().getNameRu();
            this.districtNameRo = normalizeLocation(rawDistrictRo);
            this.districtNameRu = normalizeLocation(rawDistrictRu);
            if (isRedundantDistrict(this.nameRo, this.districtNameRo)) {
                this.districtNameRo = null;
            }
            if (isRedundantDistrict(this.nameRu, this.districtNameRu)) {
                this.districtNameRu = null;
            }
        }
        
        // Set country information
        this.countryCode = locality.getCountryCode();
        this.countryNameRo = locality.getCountryNameRo();
        this.countryNameRu = locality.getCountryNameRu();
    }
    
    public LocalityDTO(Long id, String nameRo, String nameRu, String googlePlaceId, 
                      Double latitude, Double longitude, String type, 
                      String districtNameRo, String districtNameRu, Integer searchCount) {
        this.id = id;
        this.nameRo = nameRo;
        this.nameRu = nameRu;
        this.googlePlaceId = googlePlaceId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.districtNameRo = districtNameRo;
        this.districtNameRu = districtNameRu;
        this.searchCount = searchCount;
    }

    private String normalizeLocation(String value) {
        return LocationNormalizer.normalizeIfRedundant(value);
    }

    private boolean isRedundantDistrict(String localityName, String districtName) {
        if (localityName == null || districtName == null) {
            return false;
        }
        String normalizedLocality = normalizeForCompare(localityName);
        String normalizedDistrict = normalizeForCompare(districtName);
        if (normalizedLocality.isEmpty() || normalizedDistrict.isEmpty()) {
            return false;
        }
        return normalizedDistrict.contains(normalizedLocality);
    }

    private String normalizeForCompare(String value) {
        String normalized = java.text.Normalizer.normalize(value, java.text.Normalizer.Form.NFD)
            .replaceAll("\\p{M}+", "")
            .toLowerCase();
        return normalized.replaceAll("\\s+", " ").trim();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getNameRo() {
        return nameRo;
    }
    
    public void setNameRo(String nameRo) {
        this.nameRo = nameRo;
    }
    
    public String getNameRu() {
        return nameRu;
    }
    
    public void setNameRu(String nameRu) {
        this.nameRu = nameRu;
    }
    
    public String getGooglePlaceId() {
        return googlePlaceId;
    }
    
    public void setGooglePlaceId(String googlePlaceId) {
        this.googlePlaceId = googlePlaceId;
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
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getDistrictNameRo() {
        return districtNameRo;
    }
    
    public void setDistrictNameRo(String districtNameRo) {
        this.districtNameRo = districtNameRo;
    }
    
    public String getDistrictNameRu() {
        return districtNameRu;
    }
    
    public void setDistrictNameRu(String districtNameRu) {
        this.districtNameRu = districtNameRu;
    }
    
    public Integer getSearchCount() {
        return searchCount;
    }
    
    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }
    
    public String getNameByLanguage(String language) {
        return "ru".equals(language) ? nameRu : nameRo;
    }
    
    public String getDistrictNameByLanguage(String language) {
        return "ru".equals(language) ? districtNameRu : districtNameRo;
    }
    
    // Country getters and setters
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    
    public String getCountryNameRo() {
        return countryNameRo;
    }
    
    public void setCountryNameRo(String countryNameRo) {
        this.countryNameRo = countryNameRo;
    }
    
    public String getCountryNameRu() {
        return countryNameRu;
    }
    
    public void setCountryNameRu(String countryNameRu) {
        this.countryNameRu = countryNameRu;
    }
    
    public String getCountryNameByLanguage(String language) {
        return "ru".equals(language) ? countryNameRu : countryNameRo;
    }
}
