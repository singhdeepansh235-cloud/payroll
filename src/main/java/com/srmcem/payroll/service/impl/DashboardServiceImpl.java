package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.DashboardStats;
import com.srmcem.payroll.service.DashboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Aggregates the five dashboard metrics using lightweight {@link JdbcTemplate}
 * queries against the MySQL database.
 *
 * <h3>Why JdbcTemplate instead of repositories?</h3>
 * <p>The Employee, Department, Attendance, Leave and Payroll modules are built
 * incrementally.  If a module's table does not yet exist at runtime, the helper
 * method {@link #safeCount} / {@link #safeSum} catches the SQL exception and
 * returns {@code 0}, so the dashboard never throws — it just shows zeros for
 * metrics whose modules are not yet built.
 *
 * <h3>Table / column assumptions</h3>
 * <p>These names must match the entities created in later modules.
 * Update the constants below if you choose different names.
 *
 * <pre>
 *  employees          → id, status ('ACTIVE' | 'INACTIVE')
 *  departments        → id
 *  attendance         → employee_id, attendance_date, status ('PRESENT' | 'ABSENT' | …)
 *  leave_requests     → employee_id, start_date, end_date, status ('APPROVED')
 *  payroll_records    → net_salary, payroll_month  (format: 'YYYY-MM')
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final JdbcTemplate jdbcTemplate;

    // -----------------------------------------------------------------------
    // Table / column name constants — keep in sync with future entities
    // -----------------------------------------------------------------------

    private static final String TABLE_EMPLOYEES      = "employees";
    private static final String TABLE_DEPARTMENTS    = "departments";
    private static final String TABLE_ATTENDANCE     = "attendance";
    private static final String TABLE_LEAVE          = "leave_requests";
    private static final String TABLE_PAYROLL        = "payroll_records";

    // -----------------------------------------------------------------------
    // Public API
    // -----------------------------------------------------------------------

    @Override
    public DashboardStats getStats() {
        String today       = LocalDate.now().toString();                   // "yyyy-MM-dd"
        String monthPrefix = today.substring(0, 7);                        // "yyyy-MM"

        return DashboardStats.builder()
                .totalEmployees(countEmployees())
                .totalDepartments(countDepartments())
                .presentToday(countPresentToday(today))
                .onLeaveToday(countOnLeaveToday(today))
                .totalPayrollCurrentMonth(sumPayrollThisMonth(monthPrefix))
                .build();
    }

    // -----------------------------------------------------------------------
    // Individual metric queries
    // -----------------------------------------------------------------------

    /** Total active employees. */
    private long countEmployees() {
        return safeCount(
            "SELECT COUNT(*) FROM " + TABLE_EMPLOYEES + " WHERE status = 'ACTIVE'"
        );
    }

    /** Total departments. */
    private long countDepartments() {
        return safeCount(
            "SELECT COUNT(*) FROM " + TABLE_DEPARTMENTS
        );
    }

    /**
     * Employees with an attendance record of 'PRESENT' for the given date.
     *
     * @param date ISO date string "yyyy-MM-dd"
     */
    private long countPresentToday(String date) {
        return safeCount(
            "SELECT COUNT(*) FROM " + TABLE_ATTENDANCE
            + " WHERE attendance_date = '" + date + "' AND status = 'PRESENT'"
        );
    }

    /**
     * Employees whose approved leave spans today.
     *
     * @param date ISO date string "yyyy-MM-dd"
     */
    private long countOnLeaveToday(String date) {
        return safeCount(
            "SELECT COUNT(*) FROM " + TABLE_LEAVE
            + " WHERE status = 'APPROVED'"
            + "   AND start_date <= '" + date + "'"
            + "   AND end_date   >= '" + date + "'"
        );
    }

    /**
     * Sum of {@code net_salary} for all payroll records in the current month.
     *
     * @param monthPrefix "yyyy-MM"
     */
    private BigDecimal sumPayrollThisMonth(String monthPrefix) {
        return safeSum(
            "SELECT COALESCE(SUM(net_salary), 0) FROM " + TABLE_PAYROLL
            + " WHERE payroll_month = '" + monthPrefix + "'"
        );
    }

    // -----------------------------------------------------------------------
    // Safe query helpers — return 0 if the table doesn't exist yet
    // -----------------------------------------------------------------------

    /**
     * Executes a {@code SELECT COUNT(*)} query.
     * Returns {@code 0L} if the table does not yet exist (e.g., module not built).
     */
    private long safeCount(String sql) {
        try {
            Long result = jdbcTemplate.queryForObject(sql, Long.class);
            return result != null ? result : 0L;
        } catch (Exception ex) {
            log.debug("Dashboard count query skipped (table may not exist yet): {}", ex.getMessage());
            return 0L;
        }
    }

    /**
     * Executes a {@code SELECT SUM()} query.
     * Returns {@link BigDecimal#ZERO} if the table does not yet exist.
     */
    private BigDecimal safeSum(String sql) {
        try {
            BigDecimal result = jdbcTemplate.queryForObject(sql, BigDecimal.class);
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception ex) {
            log.debug("Dashboard sum query skipped (table may not exist yet): {}", ex.getMessage());
            return BigDecimal.ZERO;
        }
    }
}
