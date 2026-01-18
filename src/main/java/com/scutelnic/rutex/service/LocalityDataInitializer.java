package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.District;
import com.scutelnic.rutex.entity.Locality;
import com.scutelnic.rutex.repository.DistrictRepository;
import com.scutelnic.rutex.repository.LocalityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Component
public class LocalityDataInitializer implements CommandLineRunner {
    
    private static final Logger logger = LoggerFactory.getLogger(LocalityDataInitializer.class);
    
    private final DistrictRepository districtRepository;
    private final LocalityRepository localityRepository;
    
    public LocalityDataInitializer(DistrictRepository districtRepository, LocalityRepository localityRepository) {
        this.districtRepository = districtRepository;
        this.localityRepository = localityRepository;
    }
    
    @Override
    public void run(String... args) throws Exception {
        logger.info("LocalityDataInitializer starting...");
        
        if (districtRepository.count() == 0) {
            logger.info("No districts found, initializing districts...");
            initializeDistricts();
        }
        
        checkAndAddMissingLocalities();
        logger.info("LocalityDataInitializer finished.");
    }
    
    private void initializeDistricts() {
        List<District> districts = Arrays.asList(
            createDistrict("Chișinău", "Кишинёв"),
            createDistrict("Bălți", "Бельцы"),
            createDistrict("Anenii Noi", "Анений Ной"),
            createDistrict("Basarabeasca", "Бессарабка"),
            createDistrict("Briceni", "Бричаны"),
            createDistrict("Cahul", "Кагул"),
            createDistrict("Călărași", "Кэлэрашь"),
            createDistrict("Cantemir", "Кантемир"),
            createDistrict("Căușeni", "Кэушень"),
            createDistrict("Cimișlia", "Чимишлия"),
            createDistrict("Criuleni", "Криулень"),
            createDistrict("Dondușeni", "Дондюшены"),
            createDistrict("Drochia", "Дрокия"),
            createDistrict("Dubăsari", "Дубоссары"),
            createDistrict("Edineț", "Единец"),
            createDistrict("Fălești", "Фэлешть"),
            createDistrict("Florești", "Флорешть"),
            createDistrict("Glodeni", "Глодень"),
            createDistrict("Hîncești", "Хынчешть"),
            createDistrict("Ialoveni", "Яловены"),
            createDistrict("Leova", "Леова"),
            createDistrict("Nisporeni", "Ниспорень"),
            createDistrict("Ocnița", "Окница"),
            createDistrict("Orhei", "Орхей"),
            createDistrict("Rezina", "Резина"),
            createDistrict("Rîșcani", "Рышканы"),
            createDistrict("Sîngerei", "Сынжерей"),
            createDistrict("Șoldănești", "Шолдэнешть"),
            createDistrict("Soroca", "Сорока"),
            createDistrict("Strășeni", "Стрэшень"),
            createDistrict("Ștefan Vodă", "Штефан Водэ"),
            createDistrict("Taraclia", "Тараклия"),
            createDistrict("Telenești", "Теленешть"),
            createDistrict("Ungheni", "Унгень")
        );
        
        districtRepository.saveAll(districts);
        logger.info("Initialized {} districts", districts.size());
    }
    
    private District createDistrict(String nameRo, String nameRu) {
        District district = new District();
        district.setNameRo(nameRo);
        district.setNameRu(nameRu);
        return district;
    }
    
    private void checkAndAddMissingLocalities() {
        logger.info("Checking for missing localities...");
        
        // Verifică dacă Seliște din Nisporeni există
        List<District> allDistricts = districtRepository.findAll();
        logger.info("Found {} districts in database", allDistricts.size());
        
        Optional<District> nisporeniDistrict = allDistricts.stream()
                .filter(d -> "Nisporeni".equals(d.getNameRo()))
                .findFirst();
        
        if (nisporeniDistrict.isPresent()) {
            logger.info("Found Nisporeni district with ID: {}", nisporeniDistrict.get().getId());
            
            // Mai întâi adaugă Raionul Nisporeni dacă nu există
            List<Locality> existingRaion = localityRepository.findByNameRoContainingIgnoreCase("Raionul Nisporeni");
            boolean raionExists = existingRaion.stream()
                    .anyMatch(locality -> locality.getDistrict() != null && 
                                        locality.getDistrict().getId().equals(nisporeniDistrict.get().getId()));
            
            if (!raionExists) {
                logger.info("Adding Raionul Nisporeni...");
                createLocality(nisporeniDistrict.get(), "Raionul Nisporeni", "Район Ниспорень", 47.0833, 28.1833, Locality.LocalityType.CITY);
                logger.info("Raionul Nisporeni added successfully");
            } else {
                logger.info("Raionul Nisporeni already exists");
            }
            
            // Apoi adaugă Seliște dacă nu există
            List<Locality> existingSeliste = localityRepository.findByNameRoContainingIgnoreCase("Seliște");
            boolean selisteExists = existingSeliste.stream()
                    .anyMatch(locality -> locality.getDistrict() != null && 
                                        locality.getDistrict().getId().equals(nisporeniDistrict.get().getId()));
            
            if (!selisteExists) {
                logger.info("Adding Seliște...");
                createLocality(nisporeniDistrict.get(), "Seliște, Raionul Nisporeni", "Селиште, Район Ниспорень", 47.1000, 28.2000, Locality.LocalityType.VILLAGE);
                logger.info("Seliște added successfully");
            } else {
                logger.info("Seliște already exists");
            }
        } else {
            logger.warn("Nisporeni district not found in database!");
        }
    }
    
    private void createLocality(District district, String nameRo, String nameRu, 
                               double latitude, double longitude, Locality.LocalityType type) {
        Locality locality = new Locality();
        locality.setNameRo(nameRo);
        locality.setNameRu(nameRu);
        locality.setLatitude(latitude);
        locality.setLongitude(longitude);
        locality.setType(type);
        locality.setDistrict(district);
        locality.setSearchCount(0);
        
        locality.setCountryCode("MD");
        locality.setCountryNameRo("Moldova");
        locality.setCountryNameRu("Молдова");
        
        localityRepository.save(locality);
    }
}
