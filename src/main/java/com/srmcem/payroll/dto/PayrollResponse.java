package com.srmcem.payroll.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Read-only projection of a PayrollRecord returned to the client.
 *
 * <p>Employee details are flattened (id + full name) to avoid extra round-trips.
 * {@code payrollMonth} is returned in human-readable {@code "MMMM-yyyy"} format
 * (e.g. {@code "July-2026"}).
 */
@Data
@Builder
public class PayrollResponse {

    private Long   payrollId;
    private Long   employeeId;
    private String employeeName;

    /** Human-readable period, e.g. "July-2026". */
    private String payrollMonth;

    private BigDecimal basicSalary;
    private BigDecimal bonus;
    private BigDecimal overtime;
    private BigDecimal deductions;

    /** grossSalary = basicSalary + bonus + overtime */
    private BigDecimal grossSalary;

    /** netSalary = grossSalary − deductions */
    private BigDecimal netSalary;
}
