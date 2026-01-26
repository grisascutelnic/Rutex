package com.scutelnic.rutex.service;

import com.scutelnic.rutex.entity.User;
import com.scutelnic.rutex.entity.Role;
import com.scutelnic.rutex.repository.UserRepository;
import com.scutelnic.rutex.repository.RoleRepository;
import com.scutelnic.rutex.service.CloudinaryService;
import com.scutelnic.rutex.dto.LoginRequest;
import com.scutelnic.rutex.dto.RegisterRequest;
import com.scutelnic.rutex.dto.AuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private NotificationService notificationService;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    /**
     * Returnează ultimii 70 de utilizatori creați, ordonați descrescător după data creării
     * @return Lista cu ultimii 70 de utilizatori
     */
    public List<User> getRecentUsers() {
        return userRepository.findTop70ByOrderByCreatedAtDesc();
    }
    
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
    
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmailAndIsActiveTrue(email);
    }
    
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Utilizatorul cu acest email există deja");
        }
        User savedUser = userRepository.save(user);
        notificationService.createWelcomeNotification(savedUser);
        return savedUser;
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    public User updateProfile(Long userId, String firstName, String lastName, String email, 
                            String phone, String phonePrefix, String currentPassword, String newPassword, 
                            String profileImageUrl) {
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit"));
        
        // Verificăm dacă email-ul nou nu este folosit de alt utilizator
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new RuntimeException("Un utilizator cu acest email există deja");
        }
        
        // Normalizăm numărul de telefon (fără prefix)
        String normalizedPhone = normalizePhoneNumber(phone);
        
        // Actualizăm informațiile de bază
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setPhone(normalizedPhone);
        user.setPhonePrefix(phonePrefix);
        
        // Verificăm schimbarea parolei
        if (newPassword != null && !newPassword.isEmpty()) {
            if (currentPassword == null || currentPassword.isEmpty()) {
                throw new RuntimeException("Parola actuală este obligatorie pentru a schimba parola");
            }
            
            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                throw new RuntimeException("Parola actuală este incorectă");
            }
            
            if (newPassword.length() < 6) {
                throw new RuntimeException("Parola nouă trebuie să aibă cel puțin 6 caractere");
            }
            
            user.setPassword(passwordEncoder.encode(newPassword));
        }
        
        // Gestionăm imaginea de profil
        if (profileImageUrl != null && !profileImageUrl.trim().isEmpty()) {
            user.setProfileImage(profileImageUrl);
        }
        
        return userRepository.save(user);
    }
    

    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit"));
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    public AuthResponse login(LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            return new AuthResponse(false, "Email și parola sunt obligatorii");
        }
        
        Optional<User> userOpt = userRepository.findByEmailAndIsActiveTrue(loginRequest.getEmail());
        if (userOpt.isEmpty()) {
            return new AuthResponse(false, "Email sau parolă incorectă");
        }
        
        User user = userOpt.get();
        // Verificăm parola folosind BCrypt
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return new AuthResponse(false, "Email sau parolă incorectă");
        }
        
        return new AuthResponse(true, "Autentificare reușită", user);
    }
    
    public AuthResponse register(RegisterRequest registerRequest) {
        System.out.println("=== REGISTRATION START ===");
        System.out.println("Starting registration for email: " + registerRequest.getEmail());
        
        System.out.println("Validating required fields:");
        System.out.println("Email: " + (registerRequest.getEmail() != null ? "present" : "null"));
        System.out.println("Password: " + (registerRequest.getPassword() != null ? "present" : "null"));
        System.out.println("FirstName: " + (registerRequest.getFirstName() != null ? "present" : "null"));
        System.out.println("LastName: " + (registerRequest.getLastName() != null ? "present" : "null"));
        System.out.println("Phone: " + (registerRequest.getPhone() != null ? "present" : "null"));
        
        if (registerRequest.getEmail() == null || registerRequest.getPassword() == null ||
            registerRequest.getFirstName() == null || registerRequest.getLastName() == null ||
            registerRequest.getPhone() == null) {
            System.out.println("Registration failed: Missing required fields");
            System.out.println("=== REGISTRATION FAILED - MISSING FIELDS ===");
            return new AuthResponse(false, "Toate câmpurile sunt obligatorii");
        }
        
        System.out.println("Checking if email exists: " + registerRequest.getEmail());
        boolean emailExists = userRepository.existsByEmail(registerRequest.getEmail());
        System.out.println("Email exists check result: " + emailExists);
        if (emailExists) {
            System.out.println("Registration failed: Email already exists");
            System.out.println("=== REGISTRATION FAILED - EMAIL EXISTS ===");
            return new AuthResponse(false, "Un utilizator cu acest email există deja");
        }
        
        try {
            System.out.println("Creating new User object...");
            User newUser = new User();
            newUser.setEmail(registerRequest.getEmail());
            newUser.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
            newUser.setFirstName(registerRequest.getFirstName());
            newUser.setLastName(registerRequest.getLastName());
            
            // Salvăm prefixul și numărul separat
            String phonePrefix = registerRequest.getPhonePrefix();
            String phoneNumber = normalizePhoneNumber(registerRequest.getPhone());
            
            System.out.println("=== PHONE PROCESSING ===");
            System.out.println("Original phone: " + registerRequest.getPhone());
            System.out.println("PhonePrefix from request: " + phonePrefix);
            System.out.println("Normalized phone: " + phoneNumber);
            System.out.println("Setting phonePrefix to: " + phonePrefix);
            
            newUser.setPhonePrefix(phonePrefix);
            newUser.setPhone(phoneNumber);
            
            newUser.setProfileImage(registerRequest.getProfileImage());
            
            System.out.println("User object created with email: " + newUser.getEmail());
            
            // Adăugăm rolul ROLE_USER direct la utilizator înainte de salvare
            System.out.println("Looking for ROLE_USER in database...");
            Role userRole = roleRepository.findByName("ROLE_USER");
            System.out.println("Found user role: " + (userRole != null ? userRole.getName() : "null"));
            
            if (userRole != null) {
                System.out.println("Adding ROLE_USER to new user...");
                newUser.getRoles().add(userRole);
                System.out.println("User roles count: " + newUser.getRoles().size());
            } else {
                System.out.println("WARNING: ROLE_USER not found in database!");
            }
            
            // Salvăm utilizatorul cu rolul deja atribuit
            System.out.println("Saving user with role to database...");
            User savedUser = userRepository.save(newUser);
            System.out.println("User saved successfully with ID: " + savedUser.getId());
            System.out.println("Saved user roles count: " + savedUser.getRoles().size());
            System.out.println("Saved user phonePrefix: " + savedUser.getPhonePrefix());
            System.out.println("Saved user phone: " + savedUser.getPhone());

            notificationService.createWelcomeNotification(savedUser);
            
            System.out.println("Creating success response...");
            AuthResponse response = new AuthResponse(true, "Contul a fost creat cu succes", savedUser);
            System.out.println("Success response created: " + response.isSuccess() + " - " + response.getMessage());
            System.out.println("=== REGISTRATION SUCCESS ===");
            return response;
        } catch (Exception e) {
            System.out.println("Registration failed with exception: " + e.getMessage());
            System.out.println("Exception type: " + e.getClass().getName());
            e.printStackTrace();
            AuthResponse errorResponse = new AuthResponse(false, "Eroare la crearea contului: " + e.getMessage());
            System.out.println("Error response created: " + errorResponse.isSuccess() + " - " + errorResponse.getMessage());
            System.out.println("=== REGISTRATION FAILED ===");
            return errorResponse;
        }
    }
    
    public Map<String, Object> testDatabaseConnection() {
        Map<String, Object> result = new HashMap<>();
        try {
            long userCount = userRepository.count();
            long roleCount = roleRepository.count();
            
            // Verificăm rolurile disponibile
            List<Role> allRoles = roleRepository.findAll();
            List<String> roleNames = allRoles.stream().map(Role::getName).toList();
            
            // Verificăm utilizatorii cu rolurile lor
            List<User> allUsers = userRepository.findAll();
            List<Map<String, Object>> usersWithRoles = new ArrayList<>();
            
            for (User user : allUsers) {
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("firstName", user.getFirstName());
                userInfo.put("rolesCount", user.getRoles().size());
                userInfo.put("roles", user.getRoles().stream().map(Role::getName).toList());
                usersWithRoles.add(userInfo);
            }
            
            result.put("success", true);
            result.put("message", "Database connection successful");
            result.put("userCount", userCount);
            result.put("roleCount", roleCount);
            result.put("availableRoles", roleNames);
            result.put("usersWithRoles", usersWithRoles);
            result.put("timestamp", System.currentTimeMillis());
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "Database connection failed: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            e.printStackTrace();
        }
        return result;
    }

    public void updateUserRole(Long id, String roleName) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilizatorul nu a fost găsit"));
        
        Role role = roleRepository.findByName(roleName);
        if (role == null) {
            throw new RuntimeException("Rolul " + roleName + " nu există");
        }
        
        // Ștergem toate rolurile existente și adăugăm noul rol
        user.getRoles().clear();
        user.getRoles().add(role);
        
        userRepository.save(user);
    }
    
    /**
     * Validează și curăță numărul de telefon
     * Extrage doar cifrele din string, elimină 0-ul de la început și validează lungimea
     */
    private String normalizePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        // Eliminăm toate caracterele care nu sunt cifre
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        
        // Eliminăm 0-ul de la început dacă există (pentru numere Moldova)
        if (digitsOnly.startsWith("0") && digitsOnly.length() > 1) {
            digitsOnly = digitsOnly.substring(1);
            System.out.println("🔧 Removed leading 0 from phone number: " + phone + " -> " + digitsOnly);
        }
        
        // Verificăm dacă avem cel puțin 8 cifre (pentru numere internaționale)
        if (digitsOnly.length() < 8) {
            throw new RuntimeException("Numărul de telefon trebuie să conțină cel puțin 8 cifre");
        }
        
        // Verificăm că numărul nu este prea lung (maxim 15 cifre pentru numere internaționale)
        if (digitsOnly.length() > 15) {
            throw new RuntimeException("Numărul de telefon este prea lung. Maxim 15 cifre permise.");
        }
        
        return digitsOnly;
    }
    
    /**
     * Corectează numerele de telefon salvate greșit în baza de date
     * Această metodă va fi apelată pentru a corecta numerele existente
     */
    public String correctPhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        // Correcting phone number
        
        // Eliminăm toate caracterele care nu sunt cifre
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        // Digits extracted
        
        // Dacă numărul începe cu 0 și are 9 cifre, este un număr Moldova
        if (digitsOnly.startsWith("0") && digitsOnly.length() == 9) {
            String result = "+373 " + digitsOnly.substring(1);
            System.out.println("🔧 Corrected 0 number: " + result);
            return result;
        }
        
        // Dacă numărul începe cu 67, 62, 60, etc. și are 8 cifre, este un număr Moldova
        if (digitsOnly.length() == 8 && (digitsOnly.startsWith("6") || digitsOnly.startsWith("7"))) {
            String result = "+373 " + digitsOnly;
            // Corrected Moldova number
            return result;
        }
        
        // Dacă numărul începe cu 373 și are 11 cifre, este corect
        if (digitsOnly.startsWith("373") && digitsOnly.length() == 11) {
            String result = "+373 " + digitsOnly.substring(3);
            System.out.println("🔧 Corrected 373 number: " + result);
            return result;
        }
        
        // Pentru alte cazuri, returnăm formatul original
        return phone;
    }
    
    /**
     * Formatează numărul de telefon cu prefixul țării pentru afișare
     * @param phone - Numărul de telefon (poate fi cu sau fără prefix)
     * @return Numărul formatat cu prefix pentru afișare
     */
    public String formatPhoneForDisplay(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }
        
        System.out.println("📞 Formatting phone number: " + phone);
        
        // Eliminăm toate caracterele care nu sunt cifre
        String digitsOnly = phone.replaceAll("[^0-9]", "");
        System.out.println("📞 Digits only: " + digitsOnly);
        
        // Verificăm dacă numărul începe cu 0 (format Moldova vechi)
        if (digitsOnly.startsWith("0")) {
            // Înlocuim 0-ul cu +373 pentru compatibilitate cu numerele vechi
            String result = "+373 " + digitsOnly.substring(1);
            System.out.println("📞 Format 0: " + result);
            return result;
        }
        
        // Pentru numere care încep cu 373 (Moldova cu prefix)
        if (digitsOnly.startsWith("373")) {
            // Verificăm dacă după 373 avem cel puțin 8 cifre pentru un număr valid
            if (digitsOnly.length() >= 11) { // 373 + 8 cifre = 11
                String result = "+373 " + digitsOnly.substring(3);
                System.out.println("📞 Format 373: " + result);
                return result;
            } else {
                // Dacă nu avem suficiente cifre după 373, returnăm formatul original
                String result = "+" + digitsOnly;
                System.out.println("📞 Format 373 (short): " + result);
                return result;
            }
        }
        
        // Pentru numere care încep cu 7 (Moldova sau Rusia)
        if (digitsOnly.startsWith("7")) {
            // Verificăm dacă este un număr valid pentru Rusia (+7) - 11 cifre total
            if (digitsOnly.length() == 11) {
                String result = "+7 " + digitsOnly.substring(1);
                System.out.println("📞 Format +7: " + result);
                return result;
            } else if (digitsOnly.length() == 9) {
                // Număr Moldova cu 8 cifre după 7 (ex: 79934700 -> +373 79934700)
                String result = "+373 " + digitsOnly;
                System.out.println("📞 Format Moldova (7): " + result);
                return result;
            }
        }
        
        // Pentru numere care încep cu 6 (Moldova)
        if (digitsOnly.startsWith("6") && digitsOnly.length() == 9) {
            // Număr Moldova cu 8 cifre după 6 (ex: 67285375 -> +373 67285375)
            String result = "+373 " + digitsOnly;
            System.out.println("📞 Format Moldova (6): " + result);
            return result;
        }
        
        // Pentru alte prefixe de țări, formatăm cu +
        if (digitsOnly.length() >= 10) {
            // Presupunem că primele 1-3 cifre sunt codul de țară
            if (digitsOnly.startsWith("1")) {
                // SUA (+1)
                String result = "+1 " + digitsOnly.substring(1);
                System.out.println("📞 Format +1: " + result);
                return result;
            } else if (digitsOnly.startsWith("44")) {
                // UK (+44)
                String result = "+44 " + digitsOnly.substring(2);
                System.out.println("📞 Format +44: " + result);
                return result;
            } else if (digitsOnly.startsWith("40")) {
                // România (+40)
                String result = "+40 " + digitsOnly.substring(2);
                System.out.println("📞 Format +40: " + result);
                return result;
            } else if (digitsOnly.startsWith("380")) {
                // Ucraina (+380)
                String result = "+380 " + digitsOnly.substring(3);
                System.out.println("📞 Format +380: " + result);
                return result;
            }
        }
        
        // Pentru alte formate, returnăm cu + în față
        String result = "+" + digitsOnly;
        System.out.println("📞 Format default: " + result);
        return result;
    }

    public String maskPhoneForDisplay(String phonePrefix, String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String digitsOnly = phone.replaceAll("[^0-9]", "");
        String prefix = phonePrefix != null && !phonePrefix.trim().isEmpty() ? phonePrefix.trim() : null;
        String localNumber = digitsOnly;

        if (prefix != null) {
            String prefixDigits = prefix.replaceAll("[^0-9]", "");
            if (!prefixDigits.isEmpty() && digitsOnly.startsWith(prefixDigits) && digitsOnly.length() > prefixDigits.length()) {
                localNumber = digitsOnly.substring(prefixDigits.length());
            }
        } else {
            if (digitsOnly.startsWith("373") && digitsOnly.length() > 3) {
                prefix = "+373";
                localNumber = digitsOnly.substring(3);
            } else if (digitsOnly.startsWith("0") && digitsOnly.length() > 1) {
                prefix = "+373";
                localNumber = digitsOnly.substring(1);
            } else if (digitsOnly.startsWith("7") && digitsOnly.length() == 11) {
                prefix = "+7";
                localNumber = digitsOnly.substring(1);
            } else if (!digitsOnly.isEmpty()) {
                prefix = "+";
            }
        }

        if (prefix == null) {
            prefix = "+";
        }

        String firstPart = localNumber.length() >= 2 ? localNumber.substring(0, 2) : localNumber;
        int starCount = Math.max(3, Math.max(0, localNumber.length() - firstPart.length()));
        String maskedTail = "*".repeat(starCount);
        return prefix + firstPart + maskedTail;
    }

    public String maskEmailForDisplay(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String[] parts = email.split("@", 2);
        if (parts.length != 2) {
            return "***";
        }

        String local = parts[0];
        String domain = parts[1];
        String localMasked = local.isEmpty() ? "***" : local.substring(0, 1) + "***";

        int dotIndex = domain.indexOf('.');
        String domainMain = dotIndex > 0 ? domain.substring(0, dotIndex) : domain;
        String domainTail = dotIndex > 0 ? domain.substring(dotIndex) : "";
        String domainMasked = (domainMain.isEmpty() ? "***" : domainMain.substring(0, 1) + "***") + domainTail;

        return localMasked + "@" + domainMasked;
    }
    
    /**
     * Returnează un utilizator cu numărul de telefon formatat pentru afișare
     * @param user - Utilizatorul original
     * @return Utilizatorul cu numărul de telefon formatat
     */
    public User getUserWithFormattedPhone(User user) {
        if (user != null && user.getPhone() != null) {
            // Creăm o copie a utilizatorului pentru a nu modifica originalul
            User formattedUser = new User();
            formattedUser.setId(user.getId());
            formattedUser.setFirstName(user.getFirstName());
            formattedUser.setLastName(user.getLastName());
            formattedUser.setEmail(user.getEmail());
            
            // Folosim prefixul salvat pentru afișare
            if (user.getPhonePrefix() != null) {
                formattedUser.setPhone(user.getPhonePrefix() + " " + user.getPhone());
            } else {
                // Pentru utilizatorii vechi care nu au prefix salvat
                formattedUser.setPhone(correctPhoneNumber(user.getPhone()));
            }
            formattedUser.setPhonePrefix(user.getPhonePrefix());
            
            formattedUser.setProfileImage(user.getProfileImage());
            formattedUser.setCreatedAt(user.getCreatedAt());
            formattedUser.setAverageRating(user.getAverageRating());
            formattedUser.setRoles(user.getRoles());
            return formattedUser;
        }
        return user;
    }
}
