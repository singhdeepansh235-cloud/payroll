package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.enums.EmployeeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Employee}.
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /** Prevents duplicate email addresses across employees. */
    boolean existsByEmailIgnoreCase(String email);

    /**
     * Prevents duplicate email during update — ignores the employee being
     * edited so they can keep their existing email unchanged.
     */
    boolean existsByEmailIgnoreCaseAndEmployeeIdNot(String email, Long employeeId);

    /** All employees belonging to a given department. */
    List<Employee> findByDepartment_DepartmentId(Long departmentId);

    /** All employees holding a given designation. */
    List<Employee> findByDesignation_DesignationId(Long designationId);

    /** All employees with a specific status. */
    List<Employee> findByStatus(EmployeeStatus status);

    /**
     * Full-text search across first name, last name, and email.
     *
     * <p>Used by the Search Employee endpoint. The keyword is wrapped in
     * {@code %} wildcards at the service layer.
     */
    @Query("""
            SELECT e FROM Employee e
            WHERE LOWER(e.firstName)  LIKE LOWER(:kw)
               OR LOWER(e.lastName)   LIKE LOWER(:kw)
               OR LOWER(e.email)      LIKE LOWER(:kw)
               OR LOWER(e.phone)      LIKE LOWER(:kw)
            """)
    List<Employee> search(@Param("kw") String keyword);
}
