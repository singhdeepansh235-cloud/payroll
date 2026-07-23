package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for approving or rejecting a leave request.
 *
 * <p>Only {@code APPROVED} or {@code REJECTED} are valid target statuses.
 * Passing {@code PENDING} will be rejected by the service.
 */
@Data
public class LeaveStatusUpdateRequest {

    @NotNull(message = "Status is required (APPROVED or REJECTED)")
    @io.swagger.v3.oas.annotations.media.Schema(example = "APPROVED", description = "Target status of the leave request (APPROVED, REJECTED)")
    private LeaveStatus status;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "Approved. Ensure work handoff is completed.", description = "Approval or rejection notes from administrator")
    private String adminRemarks;
}
