package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.DepartmentRequest;
import com.srmcem.payroll.dto.DepartmentResponse;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Department module.
 *
 * <p>Base path: {@code /api/departments}
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td>  <td>/api/departments</td>       <td>Add a new department</td></tr>
 *   <tr><td>PUT</td>   <td>/api/departments/{id}</td>  <td>Update a department</td></tr>
 *   <tr><td>DELETE</td><td>/api/departments/{id}</td>  <td>Delete a department</td></tr>
 *   <tr><td>GET</td>   <td>/api/departments/{id}</td>  <td>View a department</td></tr>
 *   <tr><td>GET</td>   <td>/api/departments</td>       <td>List all departments</td></tr>
 * </table>
 *
 * <p>All endpoints require authentication (enforced by {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/departments")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Department Module", description = "Endpoints for department management")
public class DepartmentController {

    private final DepartmentService departmentService;

    // -----------------------------------------------------------------------
    // POST /api/departments — Add
    // -----------------------------------------------------------------------

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Add Department", description = "Creates a new department. Throws BadRequestException if name already exists.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Department added successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Duplicate department name or validation errors")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> addDepartment(
            @Valid @RequestBody DepartmentRequest request) {

        DepartmentResponse response = departmentService.addDepartment(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Department added successfully.", response));
    }

    // -----------------------------------------------------------------------
    // PUT /api/departments/{id} — Update
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update Department", description = "Updates fields of an existing department. Name must remain unique.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Department name is already in use"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequest request) {

        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Department updated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/departments/{id} — Delete
    // -----------------------------------------------------------------------

    @DeleteMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete Department", description = "Deletes a department from the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department deleted successfully."));
    }

    // -----------------------------------------------------------------------
    // GET /api/departments/{id} — View single
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Department by ID", description = "Fetches details of a single department.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Department fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Department not found")
    })
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(
            @PathVariable Long id) {

        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department fetched successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/departments — List all
    // -----------------------------------------------------------------------

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get All Departments", description = "Lists all departments registered in the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Departments fetched successfully")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(
                ApiResponse.success("Departments fetched successfully.", departments));
    }

    @GetMapping("/paginated")
    @io.swagger.v3.oas.annotations.Operation(summary = "Paginated Search for Departments", description = "Search departments with pagination and sorting.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated departments fetched successfully")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<DepartmentResponse>>> getDepartmentsPaginated(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departmentId") String sort,
            @RequestParam(defaultValue = "ASC") String direction) {
            
        org.springframework.data.domain.Sort.Direction dir = org.springframework.data.domain.Sort.Direction.fromString(direction);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(dir, sort));
        org.springframework.data.domain.Page<DepartmentResponse> results = departmentService.getDepartmentsPaginated(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Paginated departments fetched successfully.", results));
    }
}
