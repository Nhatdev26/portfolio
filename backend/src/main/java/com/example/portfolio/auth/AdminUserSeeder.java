package com.example.portfolio.auth;

import com.example.portfolio.user.User;
import com.example.portfolio.user.UserRepository;
import com.example.portfolio.user.UserRole;
import com.example.portfolio.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class AdminUserSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminUserSeeder.class);

    private final AdminSeedProperties properties;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public AdminUserSeeder(
            AdminSeedProperties properties,
            PasswordEncoder passwordEncoder,
            UserRepository userRepository) {
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!properties.enabled()) {
            log.info("Admin seed is disabled.");
            return;
        }

        validateSeedProperties();

        if (userRepository.existsByEmailIgnoreCaseAndDeletedAtIsNull(properties.email())) {
            log.info("Admin seed skipped because the configured admin already exists.");
            return;
        }

        User admin = new User();
        admin.setEmail(properties.email());
        admin.setPasswordHash(passwordEncoder.encode(properties.password()));
        admin.setRole(UserRole.ADMIN);
        admin.setStatus(UserStatus.ACTIVE);

        userRepository.save(admin);
        log.info("Admin seed created the initial admin account.");
    }

    private void validateSeedProperties() {
        if (!StringUtils.hasText(properties.email())) {
            throw new IllegalStateException("ADMIN_SEED_EMAIL is required when admin seed is enabled.");
        }
        if (!StringUtils.hasText(properties.password())) {
            throw new IllegalStateException("ADMIN_SEED_PASSWORD is required when admin seed is enabled.");
        }
    }
}
