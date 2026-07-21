package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.EmployeeRequest;
import com.srmcem.payroll.dto.EmployeeResponse;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Employee module.
 *
 * <p>Base path: {@code /api/employees}
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td>  <td>/api/employees</td>             <td>Add a new employee</td></tr>
 *   <tr><td>PUT</td>   <td>/api/employees/{id}</td>        <td>Update an employee</td></tr>
 *   <tr><td>DELETE</td><td>/api/employees/{id}</td>        <td>Delete an employee</td></tr>
 *   <tr><td>GET</td>   <td>/api/employees/{id}</td>        <td>View a single employee</td></tr>
 *   <tr><td>GET</td>   <td>/api/employees</td>             <td>List all employees</td></tr>
 *   <tr><td>GET</td>   <td>/api/employees/search</td>      <td>Search employees by keyword</td></tr>
 * </table>
 *
 * <p>All endpoints require authentication (enforced by {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    // -----------------------------------------------------------------------
    // POST /api/employees — Add
    // -----------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> addEmployee(
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse response = employeeService.addEmployee(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Employee added successfully.", response));
    }

    // -----------------------------------------------------------------------
    // PUT /api/employees/{id} — Update
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequest request) {

        EmployeeResponse response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Employee updated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/employees/{id} — Delete
    // -----------------------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(
                ApiResponse.success("Employee deleted successfully."));
    }

    // -----------------------------------------------------------------------
    // GET /api/employees/{id} — View single
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployeeById(
            @PathVariable Long id) {

        EmployeeResponse response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Employee fetched successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/employees — List all
    // -----------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> getAllEmployees() {
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return ResponseEntity.ok(
                ApiResponse.success("Employees fetched successfully.", employees));
    }

    // -----------------------------------------------------------------------
    // GET /api/employees/search?keyword=... — Search
    // -----------------------------------------------------------------------

    /**
     * Searches employees by a keyword matched against first name, last name,
     * email, and phone. Returns all employees when the keyword is blank.
     *
     * @param keyword optional search term
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> searchEmployees(
            @RequestParam(required = false) String keyword) {

        List<EmployeeResponse> results = employeeService.searchEmployees(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed.", results));
    }
}
