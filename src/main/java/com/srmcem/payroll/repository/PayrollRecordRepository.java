package com.srmcem.payroll.repository;

import com.srmcem.payroll.entity.PayrollRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link PayrollRecord}.
 */
@Repository
public interface PayrollRecordRepository extends JpaRepository<PayrollRecord, Long> {

    /**
     * Prevents generating duplicate payroll for the same employee in the same month.
     * {@code payrollMonth} is stored as {@code "YYYY-MM"}.
     */
    boolean existsByEmployee_EmployeeIdAndPayrollMonth(Long employeeId, String payrollMonth);

    /** Fetches the specific payroll record for an employee in a given month. */
    Optional<PayrollRecord> findByEmployee_EmployeeIdAndPayrollMonth(
            Long employeeId, String payrollMonth);

    /** Full payroll history for a given employee, newest month first. */
    List<PayrollRecord> findByEmployee_EmployeeIdOrderByPayrollMonthDesc(Long employeeId);

    /** All payroll records for a given month (e.g. for batch review). */
    List<PayrollRecord> findByPayrollMonthOrderByEmployee_FirstNameAsc(String payrollMonth);

    /** All payroll records across all employees, newest month first. */
    List<PayrollRecord> findAllByOrderByPayrollMonthDesc();

    /**
     * Sum of net salaries for a given payroll month.
     * Mirrors the dashboard SQL for cross-verification.
     */
    @Query("SELECT COALESCE(SUM(p.netSalary), 0) FROM PayrollRecord p WHERE p.payrollMonth = :month")
    BigDecimal sumNetSalaryByMonth(@Param("month") String month);
}
