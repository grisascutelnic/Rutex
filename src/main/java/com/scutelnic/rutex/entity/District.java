package com.scutelnic.rutex.entity;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "districts")
public class District {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name_ro", nullable = false)
    private String nameRo;
    
    @Column(name = "name_ru")
    private String nameRu;
    
    @Column(name = "google_place_id", unique = true)
    private String googlePlaceId;
    
    @Column(name = "latitude")
    private Double latitude;
    
    @Column(name = "longitude")
    private Double longitude;
    
    @OneToMany(mappedBy = "district", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Locality> localities;
    
    // Constructors
    public District() {}
    
    public District(String nameRo, String nameRu) {
        this.nameRo = nameRo;
        this.nameRu = nameRu;
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
    
    public List<Locality> getLocalities() {
        return localities;
    }
    
    public void setLocalities(List<Locality> localities) {
        this.localities = localities;
    }
    
    public String getNameByLanguage(String language) {
        return "ru".equals(language) ? nameRu : nameRo;
    }
}
