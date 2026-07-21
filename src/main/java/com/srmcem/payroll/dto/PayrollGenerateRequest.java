package com.srmcem.payroll.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request body for generating a monthly payroll record for a single employee.
 *
 * <p>{@code payrollMonth} must be supplied in {@code "MMMM-yyyy"} format
 * (e.g. {@code "July-2026"}) and is converted to {@code "YYYY-MM"} internally.
 *
 * <h3>Formula applied in the service</h3>
 * <pre>
 *   grossSalary = basicSalary + bonus + overtime
 *   netSalary   = grossSalary − deductions
 * </pre>
 */
@Data
public class PayrollGenerateRequest {

    @NotNull(message = "Employee ID is required")
    private Long employeeId;

    /**
     * Payroll period in {@code "MMMM-yyyy"} format (e.g. {@code "July-2026"}).
     * Converted to {@code "YYYY-MM"} for storage.
     */
    @NotBlank(message = "Payroll month is required (e.g. July-2026)")
    private String payrollMonth;

    /**
     * Override the employee's base salary for this month.
     * When null, the employee's current {@code salary} field is used.
     */
    @DecimalMin(value = "0.01", message = "Basic salary must be greater than zero")
    @Digits(integer = 10, fraction = 2, message = "Basic salary must have at most 10 integer digits and 2 decimal places")
    private BigDecimal basicSalary;

    @DecimalMin(value = "0.00", inclusive = true, message = "Bonus cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal bonus;

    @DecimalMin(value = "0.00", inclusive = true, message = "Overtime cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal overtime;

    @DecimalMin(value = "0.00", inclusive = true, message = "Deductions cannot be negative")
    @Digits(integer = 10, fraction = 2)
    private BigDecimal deductions;
}
