package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.PayrollGenerateRequest;
import com.srmcem.payroll.dto.PayrollResponse;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.PayrollService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Payroll module.
 *
 * <p>Base path: {@code /api/payroll}
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/api/payroll</td>
 *       <td>Generate payroll for one employee for a month</td></tr>
 *   <tr><td>GET</td><td>/api/payroll/{id}</td>
 *       <td>View a single payroll record</td></tr>
 *   <tr><td>GET</td><td>/api/payroll/employee/{employeeId}</td>
 *       <td>Full payroll history for an employee</td></tr>
 *   <tr><td>GET</td><td>/api/payroll/month?period=MMMM-yyyy</td>
 *       <td>All records for a given month</td></tr>
 *   <tr><td>GET</td><td>/api/payroll</td>
 *       <td>All payroll records (admin view)</td></tr>
 * </table>
 *
 * <p>All endpoints require authentication (enforced by {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final PayrollService payrollService;

    // -----------------------------------------------------------------------
    // POST /api/payroll — Generate Payroll
    // -----------------------------------------------------------------------

    /**
     * Generates a payroll record for one employee for one month.
     *
     * <p>Supply {@code payrollMonth} as {@code "MMMM-yyyy"} (e.g. {@code "July-2026"}).
     * Optional fields {@code bonus}, {@code overtime}, and {@code deductions} default to 0.
     * {@code basicSalary} defaults to the employee's stored salary when omitted.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<PayrollResponse>> generatePayroll(
            @Valid @RequestBody PayrollGenerateRequest request) {

        PayrollResponse response = payrollService.generatePayroll(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payroll generated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/payroll/{id} — View Payroll Record
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PayrollResponse>> getPayrollById(
            @PathVariable Long id) {

        PayrollResponse response = payrollService.getPayrollById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Payroll record fetched successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/payroll/employee/{employeeId} — History by Employee
    // -----------------------------------------------------------------------

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollHistoryByEmployee(
            @PathVariable Long employeeId) {

        List<PayrollResponse> records = payrollService.getPayrollHistoryByEmployee(employeeId);
        return ResponseEntity.ok(
                ApiResponse.success("Payroll history fetched successfully.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/payroll/month?period=MMMM-yyyy — Records for a Month
    // -----------------------------------------------------------------------

    /**
     * Returns all payroll records generated for the specified month.
     *
     * @param period month in {@code "MMMM-yyyy"} format, e.g. {@code "July-2026"}
     */
    @GetMapping("/month")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getPayrollByMonth(
            @RequestParam String period) {

        List<PayrollResponse> records = payrollService.getPayrollByMonth(period);
        return ResponseEntity.ok(
                ApiResponse.success("Payroll records for " + period + " fetched successfully.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/payroll — All Records (admin view)
    // -----------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAllPayrollRecords() {
        List<PayrollResponse> records = payrollService.getAllPayrollRecords();
        return ResponseEntity.ok(
                ApiResponse.success("All payroll records fetched successfully.", records));
    }
}
