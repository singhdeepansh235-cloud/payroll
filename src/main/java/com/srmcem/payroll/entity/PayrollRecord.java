package com.srmcem.payroll.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a monthly payroll record for a single employee.
 *
 * <p>Maps to the {@code payroll_records} table referenced by
 * {@code DashboardServiceImpl} which queries:
 * <pre>
 *   SELECT COALESCE(SUM(net_salary), 0)
 *   FROM payroll_records
 *   WHERE payroll_month = 'YYYY-MM'
 * </pre>
 *
 * <p>Column names {@code net_salary} and {@code payroll_month} must match
 * exactly the dashboard SQL.
 *
 * <p>A unique constraint on {@code (employee_id, payroll_month)} ensures only
 * one payroll record per employee per month.
 *
 * <h3>Salary Formula</h3>
 * <pre>
 *   grossSalary = basicSalary + bonus + overtime
 *   netSalary   = grossSalary − deductions
 * </pre>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
    name = "payroll_records",
    uniqueConstraints = @UniqueConstraint(
        name = "uk_payroll_employee_month",
        columnNames = {"employee_id", "payroll_month"}
    )
)
public class PayrollRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long payrollId;

    /** The employee this payroll record belongs to. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * Month for which payroll is generated, stored as {@code "YYYY-MM"}.
     * Column name matches the dashboard SQL {@code payroll_month}.
     */
    @Column(name = "payroll_month", nullable = false, length = 7)
    private String payrollMonth;

    /** Employee's base monthly salary (copied from {@link Employee#getSalary()} at generation time). */
    @Column(name = "basic_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal basicSalary;

    /** Additional bonus for this month (default 0.00). */
    @Column(name = "bonus", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal bonus = BigDecimal.ZERO;

    /** Overtime pay for this month (default 0.00). */
    @Column(name = "overtime", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal overtime = BigDecimal.ZERO;

    /** Total deductions (tax, PF, etc.) for this month (default 0.00). */
    @Column(name = "deductions", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal deductions = BigDecimal.ZERO;

    /**
     * grossSalary = basicSalary + bonus + overtime.
     * Column name matches dashboard if queried in future.
     */
    @Column(name = "gross_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossSalary;

    /**
     * netSalary = grossSalary − deductions.
     * Column name {@code net_salary} matches the dashboard SQL.
     */
    @Column(name = "net_salary", nullable = false, precision = 12, scale = 2)
    private BigDecimal netSalary;
}
