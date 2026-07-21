package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.Designation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Designation}.
 */
@Repository
public interface DesignationRepository extends JpaRepository<Designation, Long> {

    /** Used to prevent duplicate designation names on create. */
    boolean existsByDesignationNameIgnoreCase(String designationName);

    /**
     * Used to check for duplicate names while excluding the designation being
     * updated (so renaming to the same name is allowed).
     */
    boolean existsByDesignationNameIgnoreCaseAndDesignationIdNot(
            String designationName, Long designationId);
}
