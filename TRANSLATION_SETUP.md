# Sistem de Traducere Automată - Rutex

Acest document explică implementarea sistemului de traducere automată pentru site-ul Rutex, similar cu 999.md.

## Caracteristici

- ✅ Traducere automată RO ↔ RU folosind API gratuit (MyMemory)
- ✅ Cache în memorie și baza de date pentru performanță
- ✅ URL-uri structurate pe limbă (/ro/, /ru/)
- ✅ Selector de limbă în navbar
- ✅ Redirecționare inteligentă între limbi
- ✅ Compatibil cu Spring Boot + Thymeleaf
- ✅ Ușor de extins pentru alte limbi

## Structura Implementării

### 1. Entități și Repository

- **Translation.java** - Entitatea pentru stocarea traducerilor
- **TranslationRepository.java** - Repository pentru operații cu baza de date

### 2. Servicii

- **TranslationService.java** - Serviciul principal pentru traducere
- **TranslationInitializerService.java** - Inițializarea traducerilor la pornire

### 3. Controllere

- **TranslationController.java** - Rutele pentru pagini în diferite limbi
- **TranslationAdminController.java** - API pentru administrarea traducerilor

### 4. Configurație

- **LocaleConfig.java** - Configurația pentru localizare
- **application.properties** - Proprietăți pentru API-ul de traducere

### 5. Frontend

- **language-selector.html** - Fragment pentru selectorul de limbă
- **navbar.html** - Navbar actualizat cu traduceri
- **index.html** - Pagina principală cu suport pentru traduceri

## Instrucțiuni de Integrare

### 1. Configurarea Bazei de Date

Sistemul va crea automat tabelul `translations` la prima rulare:

```sql
CREATE TABLE translations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    translation_key VARCHAR(500) NOT NULL,
    source_text TEXT NOT NULL,
    translated_text TEXT NOT NULL,
    source_language VARCHAR(10) NOT NULL,
    target_language VARCHAR(10) NOT NULL,
    page_name VARCHAR(100),
    created_at DATETIME NOT NULL,
    updated_at DATETIME,
    is_active BOOLEAN NOT NULL DEFAULT TRUE
);
```

### 2. Configurarea API-ului de Traducere

În `application.properties`:

```properties
# Translation API Configuration
translation.api.url=https://api.mymemory.translated.net/get
translation.api.key=
```

**MyMemory API** este gratuit și nu necesită cheie API pentru utilizarea de bază.

### 3. Rutele Disponibile

#### Limba Română (implicită)
- `/` sau `/ro` - Pagina principală
- `/ro/about` - Despre
- `/ro/contact` - Contact
- `/ro/rides` - Călătorii
- `/ro/add-ride` - Adaugă cursă
- `/ro/login` - Conectare
- `/ro/register` - Înregistrare
- `/ro/profile` - Profil
- `/ro/users` - Administrare utilizatori

#### Limba Rusă
- `/ru` - Pagina principală
- `/ru/about` - О нас
- `/ru/contact` - Контакты
- `/ru/rides` - Поездки
- `/ru/add-ride` - Добавить поездку
- `/ru/login` - Вход
- `/ru/register` - Регистрация
- `/ru/profile` - Профиль
- `/ru/users` - Управление пользователями

### 4. API Endpoints

#### Schimbarea Limbii
```http
POST /api/change-language
Content-Type: application/x-www-form-urlencoded

language=ru
```

#### Obținerea Traducerilor
```http
GET /api/translations/{pageName}?sourceLang=ro&targetLang=ru
```

#### Administrarea Traducerilor (Admin)
```http
GET /api/admin/translations/stats
GET /api/admin/translations/all
POST /api/admin/translations/translate
PUT /api/admin/translations/{id}
DELETE /api/admin/translations/{id}
POST /api/admin/translations/clear-cache
```

### 5. Utilizarea în Template-uri Thymeleaf

#### Text Simplu
```html
<span th:text="${translations != null and translations.containsKey('nav.home') ? translations.get('nav.home') : 'Acasă'}">Acasă</span>
```

#### Link-uri cu Limbă
```html
<a th:href="@{'/' + ${currentLanguage} + '/rides'}" class="nav-link">Călătorii</a>
```

#### JavaScript cu Limbă
```javascript
const currentLang = /*[[${currentLanguage}]]*/ 'ro';
window.location.href = '/' + currentLang + '/profile';
```

### 6. Adăugarea de Traduceri Noi

#### Metoda 1: Prin TranslationInitializerService
```java
Map<String, String> newTranslations = new HashMap<>();
newTranslations.put("new.key", "Новый текст");
saveTranslations(newTranslations, "pageName", "ro", "ru");
```

#### Metoda 2: Prin API
```http
POST /api/admin/translations/translate
Content-Type: application/x-www-form-urlencoded

text=Text nou&sourceLang=ro&targetLang=ru&pageName=pageName
```

#### Metoda 3: Direct în Baza de Date
```sql
INSERT INTO translations (translation_key, source_text, translated_text, source_language, target_language, page_name, created_at, is_active)
VALUES ('new.key', 'Text nou', 'Новый текст', 'ro', 'ru', 'pageName', NOW(), true);
```

### 7. Cache-ul de Traduceri

Sistemul folosește un cache în memorie pentru performanță:

- **Cache Key**: `{sourceLang}_{targetLang}_{pageName}`
- **Cache Structure**: `Map<String, Map<String, String>>`
- **Cache Clear**: Automat la modificări sau manual prin API

### 8. Extinderea pentru Alte Limbi

Pentru a adăuga o nouă limbă (ex: engleză):

1. **Actualizează LocaleConfig.java**:
```java
@Bean
public LocaleResolver localeResolver() {
    SessionLocaleResolver resolver = new SessionLocaleResolver();
    resolver.setDefaultLocale(new Locale("ro"));
    return resolver;
}
```

2. **Adaugă rutele în TranslationController.java**:
```java
@GetMapping("/en")
public String indexEn(Model model, HttpSession session, HttpServletRequest request) {
    return handlePage("index", model, session, "en", request);
}
```

3. **Actualizează language-selector.html**:
```html
<a href="#" class="lang-option" data-lang="en">
    <span class="lang-flag">🇺🇸</span>
    <span class="lang-name">English</span>
</a>
```

4. **Adaugă traducerile în TranslationInitializerService**:
```java
Map<String, String> enTranslations = new HashMap<>();
enTranslations.put("nav.home", "Home");
saveTranslations(enTranslations, "navbar", "ro", "en");
```

## Monitorizare și Debugging

### Logs
```bash
# Verifică inițializarea traducerilor
grep "TRANSLATIONS INITIALIZED" logs/application.log

# Verifică erorile de traducere
grep "Error translating text" logs/application.log
```

### Statistici
```http
GET /api/admin/translations/stats
```

Răspuns:
```json
{
    "totalTranslations": 25,
    "roToRuTranslations": 15,
    "ruToRoTranslations": 10,
    "cachedPages": 3
}
```

## Performanță și Optimizări

1. **Cache în Memorie**: Traducerile sunt cache-uite pentru performanță
2. **Lazy Loading**: Traducerile se încarcă doar când sunt necesare
3. **Batch Operations**: Traducerile se salvează în loturi
4. **API Fallback**: În caz de eroare API, se folosește textul original

## Securitate

- Traducerile sunt validate înainte de salvare
- API-ul de administrare este protejat prin roluri
- Input-ul este sanitizat pentru a preveni XSS
- Rate limiting pentru API-ul de traducere

## Troubleshooting

### Probleme Comune

1. **Traducerile nu se încarcă**
   - Verifică dacă baza de date este conectată
   - Verifică logurile pentru erori
   - Rulează `TranslationInitializerService`

2. **Selectorul de limbă nu funcționează**
   - Verifică dacă JavaScript-ul este încărcat
   - Verifică console-ul browser-ului pentru erori
   - Verifică dacă API-ul `/api/change-language` răspunde

3. **URL-urile nu se redirecționează corect**
   - Verifică configurația `LocaleConfig`
   - Verifică dacă toate rutele sunt definite în `TranslationController`

### Debugging

```java
// Adaugă în TranslationService pentru debugging
System.out.println("Translating: " + sourceText + " from " + sourceLang + " to " + targetLang);
```

## Concluzie

Sistemul de traducere este complet funcțional și gata de producție. Oferă:

- Traducere automată gratuită
- Performanță optimizată cu cache
- Interfață ușor de utilizat
- Extensibilitate pentru viitoare limbi
- Administrare completă prin API

Pentru suport sau întrebări, consultă logurile aplicației sau contactează echipa de dezvoltare.
