package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "localities")
public class Locality {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name_ro", nullable = false)
    private String nameRo;
    
    @Column(name = "name_ru")
    private String nameRu;
    
    @Column(name = "google_place_id", unique = true)
    private String googlePlaceId;
    
    @Column(name = "country_code")
    private String countryCode;
    
    @Column(name = "country_name_ro")
    private String countryNameRo;
    
    @Column(name = "country_name_ru")
    private String countryNameRu;
    
    @Column(name = "latitude", nullable = false)
    private Double latitude;
    
    @Column(name = "longitude", nullable = false)
    private Double longitude;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private LocalityType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "search_count")
    private Integer searchCount = 0;
    
    // Enum pentru tipurile de localități
    public enum LocalityType {
        CITY("city"),
        TOWN("town"),
        VILLAGE("village"),
        MUNICIPALITY("municipality"),
        SUBURB("suburb"),
        NEIGHBORHOOD("neighborhood");
        
        private final String value;
        
        LocalityType(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
    
    // Constructors
    public Locality() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public Locality(String nameRo, String nameRu, Double latitude, Double longitude, LocalityType type) {
        this();
        this.nameRo = nameRo;
        this.nameRu = nameRu;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
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
    
    public LocalityType getType() {
        return type;
    }
    
    public void setType(LocalityType type) {
        this.type = type;
    }
    
    public District getDistrict() {
        return district;
    }
    
    public void setDistrict(District district) {
        this.district = district;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public Integer getSearchCount() {
        return searchCount;
    }
    
    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }
    
    public void incrementSearchCount() {
        this.searchCount++;
        this.updatedAt = LocalDateTime.now();
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
    
    public String getNameByLanguage(String language) {
        return "ru".equals(language) ? nameRu : nameRo;
    }
    
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
