package com.example.Hospital.Config;

import com.example.Hospital.Repository.MedicalPackageRepository;
import com.example.Hospital.Repository.UserRepository;
import com.example.Hospital.model.MedicalPackage;
import com.example.Hospital.model.User;
import com.example.Hospital.model.enums.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;
import java.util.List;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initDefaultUsersAndPackages(UserRepository userRepository,
                                                         MedicalPackageRepository medicalPackageRepository) {
        return args -> {
            // Seed Admin
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

            // Seed Receptionist
            boolean receptionExists = userRepository.findAll().stream()
                    .anyMatch(u -> u.getRole() == UserRole.RECEPTION);

            if (!receptionExists) {
                User reception = new User();
                reception.setName("Reception Desk");
                reception.setPhone("+918888888888");
                reception.setEmail("reception@hospital.com");
                reception.setRole(UserRole.RECEPTION);
                reception.setActive(true);
                reception.setProfileCompleted(true);

                userRepository.save(reception);
                log.info("Default RECEPTION user initialized successfully: phone=+918888888888, email=reception@hospital.com");
            }

            // Seed Initial Health Packages if none exist
            if (medicalPackageRepository.count() == 0) {
                List<MedicalPackage> initialPackages = List.of(
                        createPackage("Comprehensive Full Body Checkup",
                                "Includes Complete Blood Count (CBC), Lipid Profile, Liver Function Test (LFT), Kidney Function Test (KFT), ECG, and Physician Consultation.",
                                new BigDecimal("2499.00")),
                        createPackage("Executive Health Checkup",
                                "Comprehensive health assessment including cardiac evaluation (Echo/TMT), ultrasound abdomen, HbA1c, thyroid panel, and specialist consultation.",
                                new BigDecimal("4999.00")),
                        createPackage("Cardiac Wellness Package",
                                "Targeted cardiology package including ECG, Lipid Profile, Troponin, 2D Echocardiogram, and consultation with a Senior Cardiologist.",
                                new BigDecimal("3499.00")),
                        createPackage("Women's Wellness Package",
                                "Tailored health screening for women including Pap smear, pelvic ultrasound, bone density test (DEXA), mammogram, and Gynecologist consultation.",
                                new BigDecimal("3999.00")),
                        createPackage("Senior Citizen Care Package",
                                "Care package designed for elderly individuals covering arthritis profile, blood glucose, kidney/liver screening, eye checkup, and Geriatric consultation.",
                                new BigDecimal("2999.00"))
                );

                medicalPackageRepository.saveAll(initialPackages);
                log.info("Seeded {} initial medical packages successfully", initialPackages.size());
            }
        };
    }

    private MedicalPackage createPackage(String name, String description, BigDecimal price) {
        MedicalPackage pkg = new MedicalPackage();
        pkg.setName(name);
        pkg.setDescription(description);
        pkg.setPrice(price);
        pkg.setActive(true);
        return pkg;
    }
}
