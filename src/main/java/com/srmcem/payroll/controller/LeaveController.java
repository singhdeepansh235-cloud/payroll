package com.srmcem.payroll.controller;

import com.srmcem.payroll.dto.LeaveApplyRequest;
import com.srmcem.payroll.dto.LeaveResponse;
import com.srmcem.payroll.dto.LeaveStatusUpdateRequest;
import com.srmcem.payroll.enums.LeaveStatus;
import com.srmcem.payroll.response.ApiResponse;
import com.srmcem.payroll.service.LeaveService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Leave module.
 *
 * <p>Base path: {@code /api/leaves}
 *
 * <table border="1" cellpadding="4">
 *   <tr><th>Method</th><th>Path</th><th>Description</th></tr>
 *   <tr><td>POST</td><td>/api/leaves</td>
 *       <td>Apply for leave</td></tr>
 *   <tr><td>PATCH</td><td>/api/leaves/{id}/status</td>
 *       <td>Approve or Reject a leave request</td></tr>
 *   <tr><td>GET</td><td>/api/leaves/{id}</td>
 *       <td>View a single leave request</td></tr>
 *   <tr><td>GET</td><td>/api/leaves/employee/{employeeId}</td>
 *       <td>Leave history for an employee (optional ?status filter)</td></tr>
 *   <tr><td>GET</td><td>/api/leaves</td>
 *       <td>All leave requests (admin view)</td></tr>
 *   <tr><td>GET</td><td>/api/leaves/pending</td>
 *       <td>All PENDING leave requests (admin action queue)</td></tr>
 * </table>
 *
 * <p>All endpoints require authentication (enforced by {@code SecurityConfig}).
 */
@RestController
@RequestMapping("/api/leaves")
@RequiredArgsConstructor
@io.swagger.v3.oas.annotations.tags.Tag(name = "Leave Module", description = "Endpoints for leave requests management")
public class LeaveController {

    private final LeaveService leaveService;

    // -----------------------------------------------------------------------
    // POST /api/leaves — Apply Leave
    // -----------------------------------------------------------------------

    @PostMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Apply for Leave", description = "Applies for a new leave request. Throws BadRequestException if dates overlap with existing leaves.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Leave applied successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Overlapping leave or invalid dates range")
    })
    public ResponseEntity<ApiResponse<LeaveResponse>> applyLeave(
            @Valid @RequestBody LeaveApplyRequest request) {

        LeaveResponse response = leaveService.applyLeave(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Leave applied successfully.", response));
    }

    // -----------------------------------------------------------------------
    // PATCH /api/leaves/{id}/status — Approve / Reject
    // -----------------------------------------------------------------------

    /**
     * Approves or rejects a PENDING leave request.
     *
     * <p>Request body must carry {@code status = APPROVED} or {@code REJECTED}.
     * Optionally include {@code adminRemarks}.
     */
    @PatchMapping("/{id}/status")
    @io.swagger.v3.oas.annotations.Operation(summary = "Update Leave Status", description = "Approves or Rejects a PENDING leave request.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leave status updated successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Request is already processed or target status is PENDING"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<ApiResponse<LeaveResponse>> updateLeaveStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeaveStatusUpdateRequest request) {

        LeaveResponse response = leaveService.updateLeaveStatus(id, request);
        String message = response.getStatus() == LeaveStatus.APPROVED
                ? "Leave approved successfully."
                : "Leave rejected successfully.";
        return ResponseEntity.ok(ApiResponse.success(message, response));
    }

    // -----------------------------------------------------------------------
    // GET /api/leaves/{id} — View Single
    // -----------------------------------------------------------------------

    @GetMapping("/{id}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Leave Request by ID", description = "Fetches a single leave request details.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leave request fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Leave request not found")
    })
    public ResponseEntity<ApiResponse<LeaveResponse>> getLeaveById(@PathVariable Long id) {
        LeaveResponse response = leaveService.getLeaveById(id);
        return ResponseEntity.ok(
                ApiResponse.success("Leave request fetched successfully.", response));
    }

    // -----------------------------------------------------------------------
    // GET /api/leaves/employee/{employeeId} — Leave History
    // -----------------------------------------------------------------------

    /**
     * Returns the leave history for a specific employee.
     *
     * @param employeeId the employee
     * @param status     optional filter: {@code PENDING}, {@code APPROVED}, or {@code REJECTED}
     */
    @GetMapping("/employee/{employeeId}")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Leave History by Employee", description = "Returns full leave history for the employee, optionally filtered by status.")
    @io.swagger.v3.oas.annotations.responses.ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Leave history fetched successfully"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Employee not found")
    })
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getLeaveHistoryByEmployee(
            @PathVariable Long employeeId,
            @RequestParam(required = false) LeaveStatus status) {

        List<LeaveResponse> records = leaveService.getLeaveHistoryByEmployee(employeeId, status);
        return ResponseEntity.ok(
                ApiResponse.success("Leave history fetched successfully.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/leaves — All Requests (admin view)
    // -----------------------------------------------------------------------

    @GetMapping
    @io.swagger.v3.oas.annotations.Operation(summary = "Get All Leave Requests", description = "Lists all leave requests (for administrators).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "All leave requests fetched successfully")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getAllLeaveRequests() {
        List<LeaveResponse> records = leaveService.getAllLeaveRequests();
        return ResponseEntity.ok(
                ApiResponse.success("All leave requests fetched successfully.", records));
    }

    // -----------------------------------------------------------------------
    // GET /api/leaves/pending — Pending Queue
    // -----------------------------------------------------------------------

    @GetMapping("/pending")
    @io.swagger.v3.oas.annotations.Operation(summary = "Get Pending Leave Requests", description = "Lists all PENDING leave requests (action queue for admin).")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Pending leave requests fetched successfully")
    public ResponseEntity<ApiResponse<List<LeaveResponse>>> getPendingLeaveRequests() {
        List<LeaveResponse> records = leaveService.getPendingLeaveRequests();
        return ResponseEntity.ok(
                ApiResponse.success("Pending leave requests fetched successfully.", records));
    }

    @GetMapping("/paginated")
    @io.swagger.v3.oas.annotations.Operation(summary = "Paginated Search for Leaves", description = "Search leave requests with pagination and sorting.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Paginated leave requests fetched successfully")
    public ResponseEntity<ApiResponse<org.springframework.data.domain.Page<LeaveResponse>>> getLeavesPaginated(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appliedOn") String sort,
            @RequestParam(defaultValue = "DESC") String direction) {
            
        org.springframework.data.domain.Sort.Direction dir = org.springframework.data.domain.Sort.Direction.fromString(direction);
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, org.springframework.data.domain.Sort.by(dir, sort));
        org.springframework.data.domain.Page<LeaveResponse> results = leaveService.getLeavesPaginated(search, pageable);
        return ResponseEntity.ok(ApiResponse.success("Paginated leave requests fetched successfully.", results));
    }
}
