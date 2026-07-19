package com.srmcem.payroll.config;

import com.srmcem.payroll.entity.Admin;
import com.srmcem.payroll.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security configuration.
 *
 * <p>Strategy:
 * <ul>
 *   <li>Session-based authentication (no JWT) — suits a college project.</li>
 *   <li>CSRF disabled — safe for a REST API consumed by a front-end or Postman.</li>
 *   <li>The {@code /api/auth/**} endpoints are open; everything else requires login.</li>
 *   <li>Swagger UI is also permitted without authentication for ease of testing.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final AdminRepository adminRepository;

    // -----------------------------------------------------------------------
    // UserDetailsService — loads admin from DB for Spring Security
    // -----------------------------------------------------------------------

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            Admin admin = adminRepository.findByUsername(username)
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "Admin not found with username: " + username));

            return User.builder()
                    .username(admin.getUsername())
                    .password(admin.getPassword())   // already BCrypt-encoded
                    .roles("ADMIN")
                    .build();
        };
    }

    // -----------------------------------------------------------------------
    // Password encoder — BCrypt with default strength (10)
    // -----------------------------------------------------------------------

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // -----------------------------------------------------------------------
    // AuthenticationManager — needed if we ever wire manual auth
    // -----------------------------------------------------------------------

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // -----------------------------------------------------------------------
    // HTTP security rules
    // -----------------------------------------------------------------------

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                // Public: login endpoint
                .requestMatchers("/api/auth/**").permitAll()
                // Public: Swagger / OpenAPI docs
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()
                // Everything else requires authentication
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
