package com.srmcem.payroll.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a job designation (e.g. Software Engineer, HR Manager)
 * within the payroll system.
 *
 * <p>Maps to the {@code designations} table.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "designations")
public class Designation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long designationId;

    @Column(nullable = false, unique = true, length = 100)
    private String designationName;
}
