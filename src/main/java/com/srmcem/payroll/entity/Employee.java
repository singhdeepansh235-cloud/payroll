package com.srmcem.payroll.entity;

import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents an employee in the payroll system.
 *
 * <p>Maps to the {@code employees} table referenced by {@code DashboardServiceImpl}
 * for total-employee and present-today metrics.
 *
 * <p>Many-to-one relationships:
 * <ul>
 *   <li>{@link Department} — the organisational unit the employee belongs to</li>
 *   <li>{@link Designation} — the employee's job title / role</li>
 * </ul>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "employees")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long employeeId;

    @Column(nullable = false, length = 50)
    private String firstName;

    @Column(nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    /** The department this employee belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    /** The designation (job title) of this employee. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "designation_id", nullable = false)
    private Designation designation;

    @Column(nullable = false)
    private LocalDate joiningDate;

    /** Gross monthly salary. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal salary;

    /**
     * Employment status — stored as the enum name (ACTIVE / INACTIVE).
     * The dashboard SQL checks: {@code WHERE status = 'ACTIVE'}.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
}
