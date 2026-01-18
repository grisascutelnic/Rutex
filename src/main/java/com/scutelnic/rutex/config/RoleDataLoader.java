package com.scutelnic.rutex.config;


import com.scutelnic.rutex.entity.Role;
import com.scutelnic.rutex.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RoleDataLoader implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {

        // Verificăm dacă există roluri în baza de date
        long totalRoles = roleRepository.count();

        if (roleRepository.findByName("ROLE_ADMIN") == null) {
            Role adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
        } else {
            Role existingAdmin = roleRepository.findByName("ROLE_ADMIN");
        }

        if (roleRepository.findByName("ROLE_MOD") == null) {
            Role modRole = roleRepository.save(new Role("ROLE_MOD"));
        } else {
            Role existingMod = roleRepository.findByName("ROLE_MOD");
        }

        if (roleRepository.findByName("ROLE_USER") == null) {
            Role userRole = roleRepository.save(new Role("ROLE_USER"));
        } else {
            Role existingUser = roleRepository.findByName("ROLE_USER");
        }
        
        // Verificăm din nou după inițializare
        long finalRoles = roleRepository.count();
    }
}