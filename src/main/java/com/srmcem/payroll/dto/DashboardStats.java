package com.srmcem.payroll.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Snapshot of key metrics shown on the Admin Dashboard.
 *
 * <p>All numeric fields default to zero — the dashboard is safe to render
 * even before employee/payroll modules are populated.
 */
@Data
@Builder
public class DashboardStats {

    /** Total number of employees in the system (active + inactive). */
    private long totalEmployees;

    /** Total number of departments. */
    private long totalDepartments;

    /** Number of employees marked present today. */
    private long presentToday;

    /** Number of employees on approved leave today. */
    private long onLeaveToday;

    /**
     * Sum of net salary disbursed in the current calendar month.
     * Zero if payroll has not been processed yet.
     */
    private BigDecimal totalPayrollCurrentMonth;
}
