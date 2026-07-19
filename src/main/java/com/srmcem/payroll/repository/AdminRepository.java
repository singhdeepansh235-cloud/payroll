package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Admin}.
 *
 * <p>Spring Security's {@code UserDetailsService} needs to load an admin by
 * username at authentication time, hence the {@code findByUsername} method.
 */
@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByUsername(String username);

    boolean existsByUsername(String username);
}
