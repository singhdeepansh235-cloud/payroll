package com.srmcem.payroll.dto;

import com.srmcem.payroll.enums.LeaveStatus;
import com.srmcem.payroll.enums.LeaveType;
import lombok.Builder;
import lombok.Data;

/**
 * Read-only projection of a LeaveRequest returned to the client.
 *
 * <p>Employee details are flattened (id + full name) to avoid extra round-trips.
 * All dates are formatted as {@code dd-MM-yyyy} strings.
 */
@Data
@Builder
public class LeaveResponse {

    private Long   leaveId;
    private Long   employeeId;
    private String employeeName;

    private LeaveType   leaveType;
    private String      startDate;      // formatted dd-MM-yyyy
    private String      endDate;        // formatted dd-MM-yyyy
    private int         totalDays;
    private String      reason;

    private LeaveStatus status;
    private String      adminRemarks;
    private String      appliedOn;      // formatted dd-MM-yyyy
}
