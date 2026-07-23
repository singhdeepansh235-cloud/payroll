package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.PayrollGenerateRequest;
import com.srmcem.payroll.dto.PayrollResponse;

import java.util.List;

public interface PayrollService {

    /**
     * Generates a payroll record for the given employee and month.
     *
     * <p>Throws {@code BadRequestException} if payroll has already been
     * generated for that employee in that month.
     * Throws {@code ResourceNotFoundException} if the employee is not found.
     *
     * <p>Salary formula:
     * <pre>
     *   grossSalary = basicSalary + bonus + overtime
     *   netSalary   = grossSalary − deductions
     * </pre>
     */
    PayrollResponse generatePayroll(PayrollGenerateRequest request);

    /**
     * Fetches a single payroll record by its ID.
     * Throws {@code ResourceNotFoundException} if not found.
     */
    PayrollResponse getPayrollById(Long payrollId);

    /**
     * Returns the full payroll history for an employee, newest month first.
     *
     * @param employeeId the employee to query
     */
    List<PayrollResponse> getPayrollHistoryByEmployee(Long employeeId);

    /**
     * Returns all payroll records for a specific month.
     *
     * @param yearMonth period in {@code "MMMM-yyyy"} format (e.g. "July-2026")
     */
    List<PayrollResponse> getPayrollByMonth(String yearMonth);

    /** Returns all payroll records across all employees, newest month first. */
    List<PayrollResponse> getAllPayrollRecords();

    org.springframework.data.domain.Page<PayrollResponse> getPayrollPaginated(String search, org.springframework.data.domain.Pageable pageable);
}
