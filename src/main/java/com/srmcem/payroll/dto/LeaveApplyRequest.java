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
    private Long employeeId;

    @NotNull(message = "Leave type is required")
    private LeaveType leaveType;

    @NotBlank(message = "Start date is required (yyyy-MM-dd)")
    private String startDate;

    @NotBlank(message = "End date is required (yyyy-MM-dd)")
    private String endDate;

    @Size(max = 500, message = "Reason must not exceed 500 characters")
    private String reason;
}
