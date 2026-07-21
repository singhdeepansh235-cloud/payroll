package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.DesignationRequest;
import com.srmcem.payroll.dto.DesignationResponse;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.DesignationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Designation module.
 *
 * <p>Base path: {@code /api/designations}
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td>  <td>/api/designations</td>       <td>Add a new designation</td></tr>
 *   <tr><td>PUT</td>   <td>/api/designations/{id}</td>  <td>Update a designation</td></tr>
 *   <tr><td>DELETE</td><td>/api/designations/{id}</td>  <td>Delete a designation</td></tr>
 *   <tr><td>GET</td>   <td>/api/designations/{id}</td>  <td>View a designation</td></tr>
 *   <tr><td>GET</td>   <td>/api/designations</td>       <td>List all designations</td></tr>
 * </table>
 *
 * <p>All endpoints require authentication (enforced by {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/designations")
@RequiredArgsConstructor
public class DesignationController {

    private final DesignationService designationService;

    // -----------------------------------------------------------------------
    // POST /api/designations — Add
    // -----------------------------------------------------------------------

    @PostMapping
    public ResponseEntity<ApiResponse<DesignationResponse>> addDesignation(
            @Valid @RequestBody DesignationRequest request) {

        DesignationResponse response = designationService.addDesignation(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Designation added successfully.", response));
    }

    // -----------------------------------------------------------------------
    // PUT /api/designations/{id} — Update
    // -----------------------------------------------------------------------

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<DesignationResponse>> updateDesignation(
            @PathVariable Long id,
            @Valid @RequestBody DesignationRequest request) {

        DesignationResponse response = designationService.updateDesignation(id, request);
        return ResponseEntity.ok(
                ApiResponse.success("Designation updated successfully.", response));
    }

    // -----------------------------------------------------------------------
    // DELETE /api/designations/{id} — Delete
    // -----------------------------------------------------------------------

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.ok(
                ApiResponse.success("Designation deleted successfully."));
    }

    // -----------------------------------------------------------------------
    // GET /api/designations/{id} — View single
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<DesignationResponse>> getDesignationById(
            @PathVariable Long id) {

        DesignationResponse response = designationService.getDesignationById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Designation fetched successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/designations — List all
    // -----------------------------------------------------------------------

    @GetMapping
    public ResponseEntity<ApiResponse<List<DesignationResponse>>> getAllDesignations() {
        List<DesignationResponse> designations = designationService.getAllDesignations();
        return ResponseEntity.ok(
                ApiResponse.success("Designations fetched successfully.", designations));
    }
}
