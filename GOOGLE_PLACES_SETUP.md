# Sistem Localități cu Google Places API - DrumBun

## Descriere Generală

Sistemul implementat înlocuiește soluția OpenStreetMap cu o integrare hibridă Google Places API + cache local MySQL pentru căutarea și autocomplete-ul localităților din Moldova. Sistemul oferă:

- **Cache hibrid**: Caută mai întâi în baza locală, apoi în Google Places API
- **Suport bilingv**: Română și rusă
- **Optimizare costuri**: Reduce apelurile către Google API prin cache local
- **Experiență fluidă**: Autocomplete instant cu sugestii relevante

## Arhitectura Sistemului

### Componente Backend

1. **Entități**:
   - `District`: Raioanele din Moldova
   - `Locality`: Localitățile cu coordonate și tipuri

2. **Repository-uri**:
   - `DistrictRepository`: Operații pe raioane
   - `LocalityRepository`: Operații pe localități cu căutări avansate

3. **Servicii**:
   - `GooglePlacesService`: Integrare cu Google Places API
   - `LocalityService`: Logică de business și cache hibrid
   - `LocalityDataInitializer`: Populare inițială baza de date

4. **Controller**:
   - `LocalityController`: API REST pentru frontend

### Componente Frontend

1. **JavaScript**:
   - `LocalityAutocomplete`: Componentă modernă pentru autocomplete
   - Suport pentru navigare cu tastatura
   - Debouncing și cancelare cereri
   - Suport bilingv

2. **HTML/CSS**:
   - Pagină de test completă
   - Stiluri moderne și responsive

## Configurare Google Places API

### 1. Obținerea cheii API

1. Accesează [Google Cloud Console](https://console.cloud.google.com/)
2. Creează un proiect nou sau selectează unul existent
3. Asociază proiectul cu un cont de facturare activ
4. Activează **Places API (New)**:
   - Navighează la "APIs & Services" > "Library"
   - Caută "Places API (New)" și activează-l
5. Creează credențiale:
   - Navighează la "APIs & Services" > "Credentials"
   - Click "Create Credentials" > "API Key"
6. Restricționează cheia:
   - Click pe cheia creată
   - În "Application restrictions" selectează "IP addresses"
   - Adaugă IP-ul public static al serverului DigitalOcean
   - În "API restrictions" selectează "Restrict key" și alege "Places API (New)"

Apelurile Google Places sunt făcute de backend, nu de browser. Din acest motiv, restricția corectă este adresa IP a serverului, nu domeniul din HTTP referrer.

### 2. Configurare în Aplicație

Păstrează cheia în fișierul `.env`, care nu este inclus în Git:

```properties
GOOGLE_PLACES_API_KEY=YOUR_ACTUAL_API_KEY_HERE
```

Profilurile local și producție folosesc aceeași variabilă de mediu și endpointul Places API (New):

```properties
google.places.api.key=${GOOGLE_PLACES_API_KEY}
google.places.api.base-url=https://places.googleapis.com/v1
```

### 3. Limitări și costuri

Consultă pagina oficială Google Maps Platform pentru prețurile și pragurile actuale. Configurează limite de cotă și alerte de buget înainte de activarea în producție.

**Strategia de optimizare**:
- Cache local pentru localitățile deja cunoscute
- Field masks pentru a solicita numai datele necesare
- Debouncing pe frontend

## Endpoints API

### Autocomplete
```
GET /api/localities/autocomplete?query=chi&language=ro&limit=10
```

### Căutare Completă
```
GET /api/localities/search?query=chisinau&language=ro&limit=20
```

### Detalii Localitate
```
GET /api/localities/{id}
GET /api/localities/google-place/{googlePlaceId}
```

### Localități Populare
```
GET /api/localities/popular?limit=10
```

### Statistici
```
GET /api/localities/stats/total
GET /api/localities/stats/district/{districtId}
```

### Căutare Geografică
```
GET /api/localities/bounding-box?minLat=47.0&maxLat=47.1&minLng=28.8&maxLng=28.9
```

## Utilizare Frontend

### Inițializare Autocomplete

```javascript
// Inițializare de bază
const autocomplete = new LocalityAutocomplete({
    inputSelector: '#locality-input',
    language: 'ro',
    limit: 10
});

// Event listener pentru selecție
document.addEventListener('localitySelected', function(e) {
    const locality = e.detail.locality;
    console.log('Selected:', locality.nameByLanguage('ro'));
});
```

### Opțiuni Avansate

```javascript
const autocomplete = new LocalityAutocomplete({
    inputSelector: '.locality-input',
    resultsContainerSelector: '.locality-results',
    apiBaseUrl: '/api/localities',
    language: 'ro',
    limit: 15,
    minQueryLength: 2,
    debounceDelay: 300
});
```

### Schimbare Limbă

```javascript
// Schimbare limbă dinamic
autocomplete.setLanguage('ru');

// Sau prin selector
document.querySelector('.language-selector').addEventListener('change', function(e) {
    autocomplete.setLanguage(e.target.value);
});
```

## Schema Bazei de Date

### Tabela `districts`
```sql
CREATE TABLE districts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_ro VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    google_place_id VARCHAR(255) UNIQUE,
    latitude DOUBLE,
    longitude DOUBLE
);
```

### Tabela `localities`
```sql
CREATE TABLE localities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name_ro VARCHAR(255) NOT NULL,
    name_ru VARCHAR(255),
    google_place_id VARCHAR(255) UNIQUE,
    latitude DOUBLE NOT NULL,
    longitude DOUBLE NOT NULL,
    type ENUM('CITY', 'TOWN', 'VILLAGE', 'MUNICIPALITY', 'SUBURB', 'NEIGHBORHOOD'),
    district_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    search_count INT DEFAULT 0,
    FOREIGN KEY (district_id) REFERENCES districts(id)
);
```

## Populare Inițială Baza de Date

Sistemul include un `LocalityDataInitializer` care:

1. **Creează toate raioanele** din Moldova (35 raioane)
2. **Populează localitățile principale** pentru fiecare raion
3. **Include coordonate GPS** exacte pentru localități
4. **Suportă nume bilingve** (română + rusă)

Pentru a rula popularea:
```bash
# Aplicația va popula automat la primul start
mvn spring-boot:run
```

## Testare Sistem

### Pagină de Test
Accesează: `http://localhost:8080/test-localities`

Funcționalități de test:
- ✅ Autocomplete în timp real
- ✅ Schimbare limbă (RO/RU)
- ✅ Căutare avansată
- ✅ Test API endpoints
- ✅ Statistici sistem

### Exemple de Testare

```bash
# Test autocomplete
curl "http://localhost:8080/api/localities/autocomplete?query=chi&language=ro&limit=5"

# Test căutare completă
curl "http://localhost:8080/api/localities/search?query=orhei&language=ro&limit=10"

# Test localități populare
curl "http://localhost:8080/api/localities/popular?limit=5"

# Test statistici
curl "http://localhost:8080/api/localities/stats/total"
```

## Optimizări și Performanță

### Cache Hibrid
- **Prima căutare**: Google Places API + salvare local
- **Căutări ulterioare**: Doar baza locală
- **Reducere costuri**: ~80% mai puține apeluri API

### Filtrare Geografică
- **Bounding box Moldova**: 45.47° - 48.47° lat, 26.62° - 30.14° lng
- **Filtrare automată**: Doar localități din Moldova
- **Precizie îmbunătățită**: Rezultate relevante

### Frontend Optimizări
- **Debouncing**: 300ms între cereri
- **Cancelare cereri**: Anulează cererile anterioare
- **Cache browser**: Rezultate în localStorage
- **Lazy loading**: Încarcă doar când e necesar

## Mentenanță și Actualizare

### Actualizare Date
```java
// Rulare manuală actualizare
@Autowired
private LocalityDataInitializer initializer;

// Actualizează toate localitățile
initializer.updateAllLocalities();
```

### Monitorizare
- **Logs**: Toate apelurile API sunt logate
- **Statistici**: Număr localități, cache hit rate
- **Erori**: Tratare graceful pentru API failures

### Backup și Restore
```sql
-- Backup localități
mysqldump -u root -p haidavai_db localities districts > localities_backup.sql

-- Restore
mysql -u root -p haidavai_db < localities_backup.sql
```

## Troubleshooting

### Probleme Comune

1. **API Key Invalid**
   ```
   Error: Google Places API returned status: REQUEST_DENIED
   Soluție: Verifică cheia API și restricțiile
   ```

2. **Rate Limiting**
   ```
   Error: Google Places API returned status: OVER_QUERY_LIMIT
   Soluție: Așteaptă sau upgrade la plan plătit
   ```

3. **Locații în afara Moldovei**
   ```
   Warning: Location is outside Moldova bounds
   Soluție: Verifică filtrarea geografică
   ```

### Debug Mode
```properties
# Activează logging detaliat
logging.level.com.scutelnic.rutex.service.GooglePlacesService=DEBUG
logging.level.com.scutelnic.rutex.service.LocalityService=DEBUG
```

## Securitate

### Protecție API Key
- ✅ Restricție la IP-ul public al serverului
- ✅ Restricții API (doar Places API (New))
- ✅ Cheia este transmisă în header și nu este scrisă în loguri
- ✅ Monitorizare utilizare
- ✅ Rate limiting pe server

### Validare Input
- ✅ Sanitizare query-uri
- ✅ Validare coordonate
- ✅ Protecție SQL injection (JPA)

## Costuri Estimative

### Pentru 1000 utilizatori/zi:
- **Autocomplete requests**: ~5000/zi
- **Details requests**: ~1000/zi
- **Cost total**: ~$15/lună

### Optimizări pentru reducerea costurilor:
- Cache local reduce cu 80%
- Debouncing reduce cu 50%
- Filtrare geografică reduce cu 30%

## Concluzie

Sistemul implementat oferă o soluție completă și optimizată pentru căutarea localităților din Moldova, combinând avantajele Google Places API cu eficiența unui cache local. Experiența utilizatorului este fluidă și costurile sunt optimizate prin strategii inteligente de cache și filtrare.
