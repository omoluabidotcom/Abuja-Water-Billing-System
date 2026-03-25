package com.hackhaton.fctwaterbilling.config;

import com.hackhaton.fctwaterbilling.entity.SystemUser;
import com.hackhaton.fctwaterbilling.enums.UserRole;
import com.hackhaton.fctwaterbilling.enums.UserStatus;
import com.hackhaton.fctwaterbilling.repository.SystemUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final SystemUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        seedAdminUser();
    }

    private void seedAdminUser() {
        if (userRepository.findByUsername("admin").isPresent()) {
            log.info("Admin user already exists — skipping seed.");
            return;
        }

        SystemUser admin = SystemUser.builder()
                .username("admin")
                .passwordHash(passwordEncoder.encode("admin123"))
                .role(UserRole.ADMIN)
                .status(UserStatus.ACTIVE)
                .createdBy("system")
                .build();

        userRepository.save(admin);
        log.info("Default admin user created successfully.");
    }
}

