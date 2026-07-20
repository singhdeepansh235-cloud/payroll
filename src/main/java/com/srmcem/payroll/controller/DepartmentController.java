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
public class DepartmentController {

    private final DepartmentService departmentService;

    // -----------------------------------------------------------------------
    // POST /api/departments — Add
    // -----------------------------------------------------------------------

    @PostMapping
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
    public ResponseEntity<ApiResponse<Void>> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(
                ApiResponse.success("Department deleted successfully."));
    }

    // -----------------------------------------------------------------------
    // GET /api/departments/{id} — View single
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
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
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        return ResponseEntity.ok(
                ApiResponse.success("Departments fetched successfully.", departments));
    }
}
