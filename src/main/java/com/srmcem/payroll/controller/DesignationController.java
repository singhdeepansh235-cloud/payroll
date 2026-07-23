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
@io.swagger.v3.oas.annotations.tags.Tag(name = "Designation Module", description = "Endpoints for designation management")
public class DesignationController {

    private final DesignationService designationService;

    // -----------------------------------------------------------------------
    // POST /api/designations — Add
    // -----------------------------------------------------------------------

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Add Designation", description = "Creates a new designation. Throws BadRequestException if name already exists.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Designation added successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Duplicate designation name or validation errors")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Update Designation", description = "Updates fields of an existing designation. Name must remain unique.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Designation name is already in use"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Delete Designation", description = "Deletes a designation from the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation deleted successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
    public ResponseEntity<ApiResponse<Void>> deleteDesignation(@PathVariable Long id) {
        designationService.deleteDesignation(id);
        return ResponseEntity.ok(
                ApiResponse.success("Designation deleted successfully."));
    }

    // -----------------------------------------------------------------------
    // GET /api/designations/{id} — View single
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Designation by ID", description = "Fetches details of a single designation.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designation fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Designation not found")
    })
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
    @io.swagger.v3.oas.annotations.Operation(summary = "Get All Designations", description = "Lists all designations registered in the system.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Designations fetched successfully")
    public ResponseEntity<ApiResponse<List<DesignationResponse>>> getAllDesignations() {
        List<DesignationResponse> designations = designationService.getAllDesignations();
        return ResponseEntity.ok(
                ApiResponse.success("Designations fetched successfully.", designations));
    }
}
