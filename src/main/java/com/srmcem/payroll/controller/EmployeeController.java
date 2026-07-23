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
@io.swagger.v3.oas.annotations.tags.Tag(name = "Employee Module", description = "Endpoints for employee management")
public class EmployeeController {

    private final EmployeeService employeeService;

    // -----------------------------------------------------------------------
    // POST /api/employees — Add
    // -----------------------------------------------------------------------

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Add Employee", description = "Creates a new employee profile in the system. Throws BadRequestException if email already exists.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Employee created successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid validation constraints or duplicate email")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Update Employee", description = "Updates fields of an existing employee. Email must remain unique.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid constraints or email conflict"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee, department, or designation not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete Employee", description = "Deletes an employee from the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok(
                ApiResponse.success("Employee deleted successfully."));
    }

    // -----------------------------------------------------------------------
    // GET /api/employees/{id} — View single
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Employee by ID", description = "Fetches details of a single employee.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employee details fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get All Employees", description = "Lists all employees registered in the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Employees fetched successfully")
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Search Employees", description = "Finds employees matching a keyword in first name, last name, email, or phone.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search completed successfully")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> searchEmployees(
            @RequestParam(required = false) String keyword) {

        List<EmployeeResponse> results = employeeService.searchEmployees(keyword);
        return ResponseEntity.ok(
                ApiResponse.success("Search completed.", results));
    }

    @GetMapping("/paginated")
    @io.swagger.v3.oas.annotations.Operation(summary = "Paginated Search for Employees", description = "Search employees with pagination and sorting.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated employees fetched successfully")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<EmployeeResponse>>> getEmployeesPaginated(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "employeeId") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
            
        org.springframework.data.domain.Sort.Direction dir = org.springframework.data.domain.Sort.Direction.fromString(direction);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(dir, sort));
        org.springframework.data.domain.Page<EmployeeResponse> results = employeeService.searchEmployeesPaginated(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Paginated employees fetched successfully.", results));
    }
}
