package com.srmcem.payroll.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an organisational department within the payroll system.
 *
 * <p>Maps to the {@code departments} table referenced by the
 * {@code DashboardServiceImpl} for the "Total Departments" metric.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "departments")
public class Department {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long departmentId;

    @Column(nullable = false, unique = true, length = 100)
    private String departmentName;

    @Column(length = 255)
    private String description;
}
