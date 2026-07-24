package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.PayrollGenerateRequest;
import com.srmcem.payroll.dto.PayrollResponse;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.PayrollService;
import com.srmcem.payroll.service.PayslipService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@io.swagger.v3.oas.annotations.tags.Tag(name = "Payroll Module", description = "Endpoints for generating and viewing employee payroll records")
public class PayrollController {

    private final PayrollService  payrollService;
    private final PayslipService  payslipService;

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
    @io.swagger.v3.oas.annotations.Operation(summary = "Generate Payroll Record", description = "Generates a monthly payroll record for an employee, applying earnings/deductions formula. E-mail payslip is sent automatically.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Payroll generated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Payroll already generated for this employee and period, or validation error")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Payroll Record by ID", description = "Fetches details of a single payroll record.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll record fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payroll record not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Payroll History by Employee", description = "Returns full history of payroll records for the employee.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll history fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Payroll Records for a Month", description = "Returns all employee payroll records generated for the specified month (MMMM-yyyy).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payroll records fetched successfully")
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get All Payroll Records", description = "Lists all payroll records.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All payroll records fetched successfully")
    public ResponseEntity<ApiResponse<List<PayrollResponse>>> getAllPayrollRecords() {
        List<PayrollResponse> records = payrollService.getAllPayrollRecords();
        return ResponseEntity.ok(
                ApiResponse.success("All payroll records fetched successfully.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/payroll/{id}/payslip — Download PDF Payslip
    // -----------------------------------------------------------------------

    /**
     * Generates and downloads a PDF payslip for the specified payroll record.
     *
     * <p>Returns {@code Content-Type: application/pdf} with a
     * {@code Content-Disposition: attachment} header so the browser
     * triggers a file-save dialog.
     *
     * @param id the {@code payrollId} of the target {@link com.srmcem.payroll.entity.PayrollRecord}
     */
    @GetMapping(value = "/{id}/payslip", produces = MediaType.APPLICATION_PDF_VALUE)
    @io.swagger.v3.oas.annotations.Operation(summary = "Download Payslip PDF", description = "Generates and downloads a PDF payslip for the specified payroll record.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Payslip PDF generated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Payroll record not found")
    })
    public ResponseEntity<byte[]> downloadPayslip(@PathVariable Long id) {
        byte[] pdfBytes = payslipService.generatePayslip(id);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("payslip-" + id + ".pdf")
                        .build());
        headers.setContentLength(pdfBytes.length);

        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @GetMapping("/paginated")
    @io.swagger.v3.oas.annotations.Operation(summary = "Paginated Search for Payroll Records", description = "Search payroll records with pagination and sorting.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated payroll records fetched successfully")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<PayrollResponse>>> getPayrollPaginated(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "payrollMonth") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
            
        org.springframework.data.domain.Sort.Direction dir = org.springframework.data.domain.Sort.Direction.fromString(direction);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(dir, sort));
        org.springframework.data.domain.Page<PayrollResponse> results = payrollService.getPayrollPaginated(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Paginated payroll records fetched successfully.", results));
    }
}
