package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.LeaveApplyRequest;
import com.srmcem.payroll.dto.LeaveResponse;
import com.srmcem.payroll.dto.LeaveStatusUpdateRequest;
import com.srmcem.payroll.enums.LeaveStatus;

import java.util.List;

public interface LeaveService {

    /**
     * Applies a new leave request for an employee.
     * Throws {@code BadRequestException} if dates are invalid or overlap
     * with an existing PENDING/APPROVED leave.
     */
    LeaveResponse applyLeave(LeaveApplyRequest request);

    /**
     * Approves or rejects a leave request.
     * Throws {@code BadRequestException} if the request is not in PENDING status,
     * or if the target status is not APPROVED/REJECTED.
     * Throws {@code ResourceNotFoundException} if the leave ID is not found.
     */
    LeaveResponse updateLeaveStatus(Long leaveId, LeaveStatusUpdateRequest request);

    /**
     * Fetches a single leave request by ID.
     * Throws {@code ResourceNotFoundException} if not found.
     */
    LeaveResponse getLeaveById(Long leaveId);

    /**
     * Returns the full leave history for an employee, newest first.
     *
     * @param employeeId the employee whose history to fetch
     * @param status     optional filter — pass {@code null} to return all statuses
     */
    List<LeaveResponse> getLeaveHistoryByEmployee(Long employeeId, LeaveStatus status);

    /** Returns all leave requests across all employees (admin view), newest first. */
    List<LeaveResponse> getAllLeaveRequests();

    /** Returns all leave requests with PENDING status (admin action queue). */
    List<LeaveResponse> getPendingLeaveRequests();
}
