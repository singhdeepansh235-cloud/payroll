package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.LeaveType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request body for applying a new leave request.
 *
 * <p>Dates are accepted as ISO strings ({@code "yyyy-MM-dd"}) and parsed
 * in the service layer using {@link com.srmcem.payroll.util.DateUtil#parseDate}.
 */
@Data
public class LeaveApplyRequest {

    @NotNull(message = "Employee ID is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "1", description = "ID of the employee")
    private Long employeeId;

    @NotNull(message = "Leave type is required")
    @io.swagger.v3.oas.annotations.media.Schema(example = "SICK", description = "Type of leave (SICK, CASUAL, EARNED, MATERNITY, PATERNITY, UNPAID, OTHER)")
    private LeaveType leaveType;

    @NotBlank(message = "Start date is required (yyyy-MM-dd)")
    @io.swagger.v3.oas.annotations.media.Schema(example = "2026-08-01", description = "Start date of the leave (YYYY-MM-DD)")
    private String startDate;

    @NotBlank(message = "End date is required (yyyy-MM-dd)")
    @io.swagger.v3.oas.annotations.media.Schema(example = "2026-08-03", description = "End date of the leave (YYYY-MM-DD)")
    private String endDate;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    @io.swagger.v3.oas.annotations.media.Schema(example = "Medical checkup and recovery.", description = "Reason for applying for leave")
    private String reason;
}
