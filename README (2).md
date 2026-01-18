# Rutex - Platformă de Călătorii Împărtășite

## Descriere Generală

Rutex este o platformă web inovatoare pentru călătorii împărtășite în Moldova, dezvoltată cu Spring Boot și Thymeleaf. Aplicația conectează șoferii cu călători pentru a facilita călătorii împărtășite, reducând costurile de transport și promovând sustenabilitatea.

## Caracteristici Principale

- 🚗 **Gestionare Curse**: Adăugare, editare, ștergere și căutare curse
- 👥 **Sistem Utilizatori**: Autentificare, înregistrare, profil utilizator
- 🌍 **Suport Bilingv**: Română și Rusă cu traducere automată
- 📍 **Integrare Google Places**: Autocomplete pentru localități
- ⭐ **Sistem Rating**: Evaluare utilizatori după călătorii
- 📦 **Transport Colete**: Suport pentru transport doar colete
- 🔒 **Securitate**: Spring Security, reCAPTCHA, monitorizare
- 📊 **Statistici**: Dashboard admin cu statistici detaliate

## Arhitectura Aplicației

### Backend (Spring Boot)

#### 1. Entități (Entity)

**Locația**: `src/main/java/com/scutelnic/rutex/entity/`

- **`User.java`** - Entitatea principală pentru utilizatori
  - Câmpuri: id, email, password, firstName, lastName, phone, phonePrefix, profileImage, createdAt, isActive, averageRating, totalRatings
  - Relații: ManyToMany cu Role, OneToMany cu Rating (rater și ratedUser)
  - Annotations: @Entity, @Table, Lombok (@Getter, @Setter, @NoArgsConstructor, @AllArgsConstructor)

- **`Ride.java`** - Entitatea pentru curse
  - Câmpuri: id, fromLocation, toLocation, departureTime, travelDate, availableSeats, price, description, user, createdAt, isActive, isPackageOnly, transportAndPackages
  - Relații: ManyToOne cu User
  - Suport pentru transport pasageri și colete

- **`Locality.java`** - Entitatea pentru localități
  - Câmpuri: id, nameRo, nameRu, googlePlaceId, countryCode, countryNameRo, countryNameRu, latitude, longitude, type, district, createdAt, updatedAt, searchCount
  - Enum: LocalityType (CITY, TOWN, VILLAGE, MUNICIPALITY, SUBURB, NEIGHBORHOOD)
  - Relații: ManyToOne cu District

- **`Rating.java`** - Entitatea pentru evaluări
  - Câmpuri: id, rater, ratedUser, rating, comment, createdAt
  - Relații: ManyToOne cu User (rater și ratedUser)
  - JSON properties pentru frontend

- **`Translation.java`** - Entitatea pentru traduceri
  - Câmpuri: id, translationKey, sourceText, translatedText, sourceLanguage, targetLanguage, pageName, createdAt, updatedAt, isActive
  - Suport pentru cache și traducere automată

- **`District.java`** - Entitatea pentru raioane
- **`Role.java`** - Entitatea pentru roluri utilizatori
- **`PasswordResetToken.java`** - Entitatea pentru resetare parolă
- **`RideView.java`** - Entitatea pentru vizualizări curse
- **`RideViewIP.java`** - Entitatea pentru IP-uri vizualizări
- **`SecurityEvent.java`** - Entitatea pentru evenimente securitate
- **`SiteVisitor.java`** - Entitatea pentru vizitatori site
- **`Statistics.java`** - Entitatea pentru statistici

#### 2. Repository-uri (Repository)

**Locația**: `src/main/java/com/scutelnic/rutex/repository/`

- **`UserRepository.java`** - Operații pe utilizatori
  - Metode: findByEmailAndIsActiveTrue, findTop70ByOrderByCreatedAtDesc, existsByEmail
  - Extends: JpaRepository<User, Long>

- **`RideRepository.java`** - Operații pe curse
  - Metode: findAllActiveRides, searchRidesFlexible, findByUserOrderByCreatedAtDesc, findTop5RecentRides, countByIsActiveTrue
  - Query-uri custom pentru căutare flexibilă

- **`LocalityRepository.java`** - Operații pe localități
  - Metode: findByNameRoContainingIgnoreCase, findByNameRuContainingIgnoreCase, findByGooglePlaceId
  - Suport pentru căutare în ambele limbi

- **`TranslationRepository.java`** - Operații pe traduceri
  - Metode: findBySourceTextAndLanguages, findBySourceAndTargetLanguageAndPage, countByLanguages
  - Cache și optimizare traduceri

- **`RatingRepository.java`** - Operații pe evaluări
- **`DistrictRepository.java`** - Operații pe raioane
- **`PasswordResetTokenRepository.java`** - Operații pe token-uri resetare
- **`RideViewRepository.java`** - Operații pe vizualizări
- **`RideViewIPRepository.java`** - Operații pe IP-uri vizualizări
- **`RoleRepository.java`** - Operații pe roluri
- **`SecurityEventRepository.java`** - Operații pe evenimente securitate
- **`SiteVisitorRepository.java`** - Operații pe vizitatori
- **`StatisticsRepository.java`** - Operații pe statistici

#### 3. Servicii (Service)

**Locația**: `src/main/java/com/scutelnic/rutex/service/`

- **`UserService.java`** - Serviciul principal pentru utilizatori
  - Metode: getAllUsers, getRecentUsers, getUserById, getUserByEmail, createUser, updateUser, updateProfile, deleteUser, login, register
  - Funcționalități: autentificare, înregistrare, gestionare profil, normalizare numere telefon
  - Integrare: PasswordEncoder, CloudinaryService

- **`RideService.java`** - Serviciul principal pentru curse
  - Metode: getAllActiveRides, searchRides, addRide, getRideById, getRidesByUser, getActiveRidesByUser, getCompletedRidesByUser, deleteRide, updateRide
  - Funcționalități: gestionare curse, căutare flexibilă, cleanup automat, statistici
  - Integrare: RideViewService pentru tracking vizualizări

- **`TranslationService.java`** - Serviciul pentru traduceri
  - Metode: getTranslation, translateViaApi, loadPageTranslations, getPageTranslations, clearCache, getTranslationStats
  - Funcționalități: traducere automată prin MyMemory API, cache în memorie și baza de date
  - Optimizare: cache hibrid pentru performanță

- **`LocalityService.java`** - Serviciul pentru localități
  - Integrare: GooglePlacesService pentru autocomplete
  - Cache hibrid: baza de date locală + Google Places API

- **`GooglePlacesService.java`** - Integrare Google Places API
- **`CloudinaryService.java`** - Gestionare imagini profil
- **`EmailService.java`** - Serviciu email pentru resetare parolă
- **`PasswordResetService.java`** - Gestionare resetare parolă
- **`RatingService.java`** - Gestionare evaluări utilizatori
- **`RecaptchaService.java`** - Verificare reCAPTCHA
- **`SecurityMonitoringService.java`** - Monitorizare securitate
- **`SiteVisitorService.java`** - Tracking vizitatori
- **`StatisticsService.java`** - Generare statistici
- **`RideViewService.java`** - Tracking vizualizări curse
- **`PageModelService.java`** - Serviciu pentru modele pagini
- **`TranslationInitializerService.java`** - Inițializare traduceri
- **`LocalityDataInitializer.java`** - Inițializare date localități

#### 4. Controllere (Controller)

**Locația**: `src/main/java/com/scutelnic/rutex/Controller/`

- **`AuthController.java`** - Autentificare și autorizare
  - Endpoints: POST /api/auth/login, POST /api/auth/register, POST /api/auth/logout, GET /api/auth/user, GET /api/auth/check
  - Funcționalități: login, register, logout, verificare sesiune, resetare parolă
  - Integrare: reCAPTCHA, monitorizare securitate, gestionare sesiuni

- **`RideController.java`** - Gestionare curse
  - Endpoints: GET /api/rides, GET /api/rides/{id}, GET /api/rides/search, POST /api/rides, DELETE /api/rides/{id}, PUT /api/rides/{id}
  - Funcționalități: CRUD curse, căutare, filtrare, tracking vizualizări
  - Suport: transport pasageri și colete

- **`LocalityController.java`** - Gestionare localități
  - Endpoints: GET /api/localities/search, GET /api/localities/autocomplete
  - Funcționalități: căutare localități, autocomplete, suport bilingv

- **`TranslationController.java`** - Gestionare traduceri
  - Endpoints: GET /{language}/**, POST /api/translations/translate
  - Funcționalități: rute pe limbi, traducere dinamică

- **`UserController.java`** - Gestionare utilizatori
- **`RatingController.java`** - Gestionare evaluări
- **`ContactController.java`** - Gestionare contact
- **`ProfilePageController.java`** - Pagini profil
- **`RidePageController.java`** - Pagini curse
- **`PublicPageController.java`** - Pagini publice
- **`PageController.java`** - Controller general pagini
- **`AdminStatisticsController.java`** - Statistici admin
- **`AdminUsersPageController.java`** - Gestionare utilizatori admin
- **`LanguageRedirectController.java`** - Redirecționare limbi
- **`RoleController.java`** - Gestionare roluri
- **`SecurityController.java`** - Securitate
- **`TranslationAdminController.java`** - Admin traduceri
- **`TranslationApiController.java`** - API traduceri

#### 5. DTO-uri (Data Transfer Objects)

**Locația**: `src/main/java/com/scutelnic/rutex/dto/`

- **`RideDTO.java`** - Transfer date curse
- **`UserDTO.java`** - Transfer date utilizatori
- **`LoginRequest.java`** - Cerere login
- **`RegisterRequest.java`** - Cerere înregistrare
- **`AuthResponse.java`** - Răspuns autentificare
- **`SearchRideRequest.java`** - Cerere căutare curse
- **`AddRideRequest.java`** - Cerere adăugare cursă
- **`LocalityDTO.java`** - Transfer date localități
- **`TranslationsDTO.java`** - Transfer date traduceri
- **`RatingDTO.java`** - Transfer date evaluări

#### 6. Configurații (Config)

**Locația**: `src/main/java/com/scutelnic/rutex/config/`

- **`SpringSecurityConfig.java`** - Configurație Spring Security
- **`WebConfig.java`** - Configurație web
- **`LocaleConfig.java`** - Configurație localizare
- **`CloudinaryConfig.java`** - Configurație Cloudinary
- **`RecaptchaConfig.java`** - Configurație reCAPTCHA
- **`RestTemplateConfig.java`** - Configurație RestTemplate
- **`SessionConfig.java`** - Configurație sesiuni
- **`TimezoneConfig.java`** - Configurație fus orar
- **`SchedulerConfig.java`** - Configurație scheduler
- **`GlobalExceptionHandler.java`** - Gestionare excepții globale
- **`BanInterceptor.java`** - Interceptor pentru bani
- **`VisitorTrackingInterceptor.java`** - Tracking vizitatori
- **`RoleDataLoader.java`** - Încărcare roluri

### Frontend (Thymeleaf + JavaScript)

#### 1. Template-uri HTML

**Locația**: `src/main/resources/templates/`

- **`index.html`** - Pagina principală
- **`rides.html`** - Lista curse
- **`add-ride.html`** - Adăugare cursă
- **`edit-ride.html`** - Editare cursă
- **`ride-details.html`** - Detalii cursă
- **`login.html`** - Pagina de login
- **`register.html`** - Pagina de înregistrare
- **`profile.html`** - Profil utilizator
- **`edit-profile.html`** - Editare profil
- **`about.html`** - Despre aplicație
- **`contact.html`** - Contact
- **`forgot-password.html`** - Resetare parolă
- **`reset-password.html`** - Confirmare resetare parolă
- **`users.html`** - Lista utilizatori (admin)
- **`privacy.html`** - Politică confidențialitate
- **`terms.html`** - Termeni și condiții

**Fragmente** (`fragments/`):
- **`navbar.html`** - Bara de navigare
- **`footer.html`** - Footer
- **`language-selector.html`** - Selector limbă
- **`google-analytics.html`** - Google Analytics

#### 2. JavaScript

**Locația**: `src/main/resources/static/js/`

- **`script.js`** - Script principal
- **`index.js`** - Funcționalități pagina principală
- **`rides.js`** - Gestionare curse
- **`add-ride.js`** - Adăugare cursă
- **`edit-ride.js`** - Editare cursă
- **`login.js`** - Autentificare
- **`register.js`** - Înregistrare
- **`profile.js`** - Gestionare profil
- **`edit-profile.js`** - Editare profil
- **`navbar.js`** - Funcționalități navbar
- **`contact.js`** - Formular contact
- **`forgot-password.js`** - Resetare parolă
- **`reset-password.js`** - Confirmare resetare
- **`password-toggle.js`** - Toggle parolă
- **`language-selector.js`** - Selector limbă
- **`locality-autocomplete.js`** - Autocomplete localități
- **`floating-button.js`** - Buton flotant
- **`city-translations.js`** - Traduceri orașe
- **`package-translations.js`** - Traduceri colete
- **`edit-ride-translations.js`** - Traduceri editare cursă

#### 3. CSS

**Locația**: `src/main/resources/static/css/`

- **`styles.css`** - Stiluri principale

### Baza de Date

#### Migrații (Flyway)

**Locația**: `src/main/resources/db/migration/`

- **`V1__create_localities_table.sql`** - Creare tabel localități
- **`V2__add_country_fields_to_localities.sql`** - Adăugare câmpuri țară
- **`V3__Update_Moldovan_Localities_Country_Code.sql`** - Actualizare coduri țară
- **`V4__add_package_only_to_rides.sql`** - Adăugare suport colete
- **`V5__add_transport_and_packages_to_rides.sql`** - Adăugare transport colete
- **`V6__update_existing_rides_package_fields.sql`** - Actualizare curse existente
- **`V7__force_update_all_rides_package_fields.sql`** - Forțare actualizare
- **`V8__create_ride_views_tables.sql`** - Creare tabele vizualizări
- **`V9__create_site_visitors_table.sql`** - Creare tabel vizitatori
- **`V10__add_banned_field_to_site_visitors.sql`** - Adăugare câmp ban
- **`V11__create_security_events_table.sql`** - Creare tabel evenimente securitate
- **`V12__add_phone_prefix_to_users.sql`** - Adăugare prefix telefon
- **`V13__update_existing_users_phone_prefix.sql`** - Actualizare prefixuri existente
- **`V14__create_ratings_table.sql`** - Creare tabel evaluări
- **`V15__add_rating_fields_to_users.sql`** - Adăugare câmpuri rating utilizatori
- **`V16__cleanup_duplicate_ratings.sql`** - Curățare evaluări duplicate
- **`V17__create_spring_session_tables.sql`** - Creare tabele sesiuni
- **`V18__create_statistics_table.sql`** - Creare tabel statistici
- **`V19__create_statistics_table_fix.sql`** - Corectare tabel statistici

### Configurații

#### 1. Application Properties

**Locația**: `src/main/resources/`

- **`application.properties`** - Configurație principală
- **`application-local.properties`** - Configurație locală
- **`application-prod.properties`** - Configurație producție

#### 2. Maven Configuration

**Locația**: `pom.xml`

**Dependințe principale**:
- Spring Boot 3.5.4
- Spring Security
- Spring Data JPA
- Spring Session
- Thymeleaf
- MySQL Connector
- Lombok
- Cloudinary
- Spring Mail

## Funcționalități Detaliate

### 1. Sistem de Autentificare

- **Login/Register**: Formulare cu validare și reCAPTCHA
- **Sesiuni**: Gestionare sesiuni cu timeout configurabil
- **Remember Me**: Funcționalitate "Ține-mă minte"
- **Resetare Parolă**: Email cu token de resetare
- **Securitate**: Monitorizare încercări de autentificare

### 2. Gestionare Curse

- **Adăugare Cursă**: Formular cu validare completă
- **Tipuri Curse**: Pasageri, colete, sau ambele
- **Căutare Flexibilă**: Filtrare după locație, dată, număr pasageri
- **Editare/Ștergere**: Doar proprietarul poate modifica
- **Tracking Vizualizări**: Numărare vizualizări per cursă

### 3. Sistem de Traduceri

- **Traducere Automată**: Integrare MyMemory API
- **Cache Hibrid**: Memorie + baza de date
- **Suport Bilingv**: Română și Rusă
- **URL-uri pe Limbi**: /ro/ și /ru/
- **Selector Limbă**: Schimbare dinamică

### 4. Integrare Google Places

- **Autocomplete Localități**: Sugestii în timp real
- **Cache Hibrid**: Baza de date locală + Google API
- **Suport Bilingv**: Căutare în română și rusă
- **Optimizare Costuri**: Reducere apeluri API

### 5. Sistem de Evaluări

- **Rating Utilizatori**: Evaluare 1-5 stele
- **Comentarii**: Feedback text opțional
- **Statistici**: Rating mediu și număr evaluări
- **Validare**: Doar după călătorii efectuate

### 6. Dashboard Admin

- **Statistici Utilizatori**: Număr utilizatori, activitate
- **Statistici Curse**: Curse active, completate, totale
- **Monitorizare Securitate**: Evenimente suspecte
- **Gestionare Utilizatori**: Lista, editare, dezactivare

## Instalare și Configurare

### 1. Cerințe Sistem

- Java 21+
- Maven 3.6+
- MySQL 8.0+
- Node.js (pentru build frontend)

### 2. Configurare Baza de Date

```sql
CREATE DATABASE rutex;
CREATE USER 'rutex_user'@'localhost' IDENTIFIED BY 'password';
GRANT ALL PRIVILEGES ON rutex.* TO 'rutex_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Configurare Aplicație

1. **Clonează repository-ul**:
```bash
git clone <repository-url>
cd rutex
```

2. **Configurează baza de date** în `application-local.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/rutex
spring.datasource.username=rutex_user
spring.datasource.password=password
```

3. **Configurează Google Places API**:
```properties
google.places.api.key=YOUR_API_KEY
```

4. **Configurează reCAPTCHA**:
```properties
recaptcha.secret.key=YOUR_SECRET_KEY
recaptcha.site.key=YOUR_SITE_KEY
```

5. **Configurează email**:
```properties
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
```

### 4. Rulare Aplicație

```bash
# Compilare
mvn clean compile

# Rulare
mvn spring-boot:run

# Sau
java -jar target/rutex-0.0.1-SNAPSHOT.jar
```

Aplicația va fi disponibilă la: `http://localhost:8080`

## API Endpoints

### Autentificare
- `POST /api/auth/login` - Login utilizator
- `POST /api/auth/register` - Înregistrare utilizator
- `POST /api/auth/logout` - Logout
- `GET /api/auth/user` - Utilizator curent
- `GET /api/auth/check` - Verificare autentificare
- `POST /api/auth/forgot-password` - Resetare parolă
- `POST /api/auth/reset-password` - Confirmare resetare

### Curse
- `GET /api/rides` - Lista toate cursele
- `GET /api/rides/{id}` - Detalii cursă
- `GET /api/rides/search` - Căutare curse
- `POST /api/rides` - Adăugare cursă
- `PUT /api/rides/{id}` - Actualizare cursă
- `DELETE /api/rides/{id}` - Ștergere cursă
- `GET /api/rides/my-rides` - Cursele mele
- `POST /api/rides/{id}/view` - Înregistrare vizualizare

### Localități
- `GET /api/localities/search` - Căutare localități
- `GET /api/localities/autocomplete` - Autocomplete localități

### Traduceri
- `GET /{language}/**` - Pagini pe limbi
- `POST /api/translations/translate` - Traducere text

## Structura Proiectului

```
rutex/
├── src/
│   ├── main/
│   │   ├── java/com/scutelnic/rutex/
│   │   │   ├── config/          # Configurări
│   │   │   ├── Controller/      # Controllere REST
│   │   │   ├── dto/            # Data Transfer Objects
│   │   │   ├── entity/         # Entități JPA
│   │   │   ├── repository/     # Repository-uri
│   │   │   ├── service/        # Servicii business
│   │   │   └── RutexApplication.java
│   │   └── resources/
│   │       ├── static/         # Resurse statice
│   │       │   ├── css/        # Stiluri CSS
│   │       │   ├── js/         # Scripturi JavaScript
│   │       │   └── favicon.ico
│   │       ├── templates/      # Template-uri Thymeleaf
│   │       ├── db/migration/   # Migrații Flyway
│   │       └── application*.properties
│   └── test/                   # Teste
├── uploads/                    # Fișiere încărcate
├── pom.xml                     # Configurație Maven
└── README.md                   # Documentație
```

## Tehnologii Utilizate

### Backend
- **Spring Boot 3.5.4** - Framework principal
- **Spring Security** - Autentificare și autorizare
- **Spring Data JPA** - Persistență date
- **Spring Session** - Gestionare sesiuni
- **Thymeleaf** - Template engine
- **MySQL** - Baza de date
- **Flyway** - Migrații baza de date
- **Lombok** - Reducere boilerplate code
- **Cloudinary** - Gestionare imagini
- **Spring Mail** - Trimitere email

### Frontend
- **HTML5** - Structură pagini
- **CSS3** - Stilizare
- **JavaScript (ES6+)** - Funcționalități dinamice
- **Thymeleaf** - Template engine
- **Bootstrap** - Framework CSS (implicit)

### Integrări Externe
- **Google Places API** - Autocomplete localități
- **MyMemory API** - Traducere automată
- **reCAPTCHA** - Protecție bot
- **Cloudinary** - Hosting imagini

## Dezvoltare și Contribuții

### Structura Codului

1. **Entități**: Modelele de date cu relații JPA
2. **Repository-uri**: Acces la date cu Spring Data JPA
3. **Servicii**: Logică business și operații complexe
4. **Controllere**: Endpoints REST și gestionare cereri
5. **DTO-uri**: Transfer date între layere
6. **Configurații**: Setări aplicație și integrări

### Convenții Cod

- **Naming**: camelCase pentru metode, PascalCase pentru clase
- **Packages**: Organizare pe funcționalități
- **Annotations**: Folosire Lombok pentru getters/setters
- **Documentație**: JavaDoc pentru metode publice
- **Logging**: System.out.println pentru debug

### Adăugare Funcționalități

1. **Entitate nouă**: Creează în `entity/`
2. **Repository**: Creează în `repository/`
3. **Serviciu**: Creează în `service/`
4. **Controller**: Creează în `Controller/`
5. **DTO**: Creează în `dto/` dacă necesar
6. **Template**: Creează în `templates/`
7. **JavaScript**: Creează în `static/js/`

## Monitorizare și Logging

### Logging
- **Console**: System.out.println pentru debug
- **Spring Boot**: Logging automat pentru erori
- **Custom**: Mesaje personalizate pentru operații importante

### Monitorizare
- **Securitate**: Tracking încercări de autentificare
- **Vizitatori**: Statistici vizitatori site
- **Curse**: Tracking vizualizări și activitate
- **Performanță**: Monitorizare timp răspuns

## Securitate

### Măsuri Implementate
- **Spring Security**: Autentificare și autorizare
- **reCAPTCHA**: Protecție împotriva bot-urilor
- **Password Encoding**: BCrypt pentru parole
- **Session Management**: Timeout configurabil
- **Input Validation**: Validare date de intrare
- **SQL Injection**: Protecție prin JPA
- **XSS Protection**: Escapare output Thymeleaf

### Monitorizare Securitate
- **Security Events**: Tracking evenimente suspecte
- **Failed Logins**: Monitorizare încercări eșuate
- **IP Tracking**: Urmărire adrese IP
- **Ban System**: Sistem de bani pentru utilizatori

## Performanță

### Optimizări Implementate
- **Cache**: Cache în memorie pentru traduceri
- **Database Indexing**: Indexuri pe câmpuri frecvent căutate
- **Lazy Loading**: Încărcare lazy pentru relații
- **Connection Pooling**: Pool conexiuni baza de date
- **Static Resources**: Servire optimizată resurse statice

### Monitoring
- **Database Queries**: Monitorizare query-uri lente
- **Memory Usage**: Urmărire utilizare memorie
- **Response Time**: Măsurare timp răspuns
- **Cache Hit Rate**: Eficiență cache

## Concluzie

Rutex este o aplicație web complexă și completă pentru gestionarea călătoriilor împărtășite în Moldova. Aplicația demonstrează utilizarea tehnologiilor moderne Spring Boot, implementarea unui sistem de securitate robust, integrarea cu servicii externe și dezvoltarea unei interfețe utilizator intuitive.

Arhitectura aplicației este modulară și extensibilă, permițând adăugarea ușoară de funcționalități noi. Codul este bine organizat și documentat, facilitând mentenanța și dezvoltarea ulterioară.

Pentru întrebări sau suport tehnic, contactați echipa de dezvoltare.