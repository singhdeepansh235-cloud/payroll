package com.srmcem.payroll.config;

import com.srmcem.payroll.entity.Admin;
import com.srmcem.payroll.entity.CompanySettings;
import com.srmcem.payroll.repository.AdminRepository;
import com.srmcem.payroll.repository.CompanySettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds a default Admin account on first startup if none exists.
 *
 * <p>Default credentials:
 * <pre>
 *   username : admin
 *   password : admin123
 * </pre>
 *
 * Change these immediately after the first login using the
 * {@code POST /api/auth/change-password} endpoint.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final AdminRepository adminRepository;
    private final CompanySettingsRepository settingsRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    public CommandLineRunner seedAdmin() {
        return args -> {
            if (!adminRepository.existsByUsername("admin")) {
                Admin admin = Admin.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .name("System Administrator")
                        .email("admin@payroll.com")
                        .build();

                adminRepository.save(admin);
                log.info("Default admin account created  →  username: admin  |  password: admin123");
                log.warn("Please change the default password immediately after first login!");
            } else {
                log.info("Admin account already exists — skipping seed.");
            }

            if (settingsRepository.count() == 0) {
                CompanySettings defaultSettings = CompanySettings.builder()
                        .companyName("SRMCEM Payroll System")
                        .email("hr@srmcem.com")
                        .phone("+91-0000000000")
                        .build();
                settingsRepository.save(defaultSettings);
                log.info("Default Company Settings seeded.");
            }
        };
    }
}
