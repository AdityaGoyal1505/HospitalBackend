package com.example.Hospital.Config;

import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDefaultAdmin(UserRepository userRepository) {
        return args -> {
            boolean adminExists = userRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == UserRole.ADMIN);

            if (!adminExists) {
                User admin = new User();
                admin.setName("Super Admin");
                admin.setPhone("+919999999999");
                admin.setEmail("admin@hospital.com");
                admin.setRole(UserRole.ADMIN);
                admin.setActive(true);
                admin.setProfileCompleted(true);

                userRepository.save(admin);
                log.info("Default ADMIN user initialized successfully: phone=+919999999999, email=admin@hospital.com");
            }
        };
    }
}
