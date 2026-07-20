package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Department}.
 */
@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    /** Used to prevent duplicate department names on create and update. */
    boolean existsByDepartmentNameIgnoreCase(String departmentName);

    /**
     * Used to check for duplicate names while excluding the department being
     * updated (so renaming to the same name is allowed).
     */
    boolean existsByDepartmentNameIgnoreCaseAndDepartmentIdNot(
            String departmentName, Long departmentId);

    /** Case-insensitive search by name fragment — useful for future search features. */
    List<Department> findByDepartmentNameContainingIgnoreCase(String keyword);
}
