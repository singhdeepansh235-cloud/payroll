package com.srmcem.payroll.service.impl;

import com.srmcem.payroll.dto.LeaveApplyRequest;
import com.srmcem.payroll.dto.LeaveResponse;
import com.srmcem.payroll.dto.LeaveStatusUpdateRequest;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.LeaveRequest;
import com.srmcem.payroll.enums.LeaveStatus;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.repository.LeaveRequestRepository;
import com.srmcem.payroll.service.LeaveService;
import com.srmcem.payroll.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LeaveServiceImpl implements LeaveService {

    private final LeaveRequestRepository leaveRequestRepository;
    private final EmployeeRepository     employeeRepository;

    // -----------------------------------------------------------------------
    // Apply Leave
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public LeaveResponse applyLeave(LeaveApplyRequest request) {
        Employee employee = findEmployeeOrThrow(request.getEmployeeId());

        LocalDate startDate = DateUtil.parseDate(request.getStartDate());
        LocalDate endDate   = DateUtil.parseDate(request.getEndDate());

        // Business rule: end must be on or after start
        if (endDate.isBefore(startDate)) {
            throw new BadRequestException("End date cannot be before start date.");
        }

        // Business rule: no overlap with existing PENDING/APPROVED leave
        if (leaveRequestRepository.hasOverlappingLeave(
                employee.getEmployeeId(), startDate, endDate)) {
            throw new BadRequestException(
                    "A PENDING or APPROVED leave already exists within the requested date range.");
        }

        int totalDays = (int) (startDate.until(endDate).getDays() + 1);

        LeaveRequest leaveRequest = LeaveRequest.builder()
                .employee(employee)
                .leaveType(request.getLeaveType())
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .reason(request.getReason())
                .status(LeaveStatus.PENDING)
                .appliedOn(LocalDate.now())
                .build();

        LeaveRequest saved = leaveRequestRepository.save(leaveRequest);
        log.info("Leave applied: id={}, employeeId={}, type={}, dates={} to {}, days={}",
                saved.getLeaveId(), employee.getEmployeeId(),
                saved.getLeaveType(), startDate, endDate, totalDays);
        return toResponse(saved);
    }

    // -----------------------------------------------------------------------
    // Approve / Reject Leave
    // -----------------------------------------------------------------------

    @Override
    @Transactional
    public LeaveResponse updateLeaveStatus(Long leaveId, LeaveStatusUpdateRequest request) {
        LeaveRequest leaveRequest = findLeaveOrThrow(leaveId);

        // Only PENDING leaves can be actioned
        if (leaveRequest.getStatus() != LeaveStatus.PENDING) {
            throw new BadRequestException(
                    "Leave request is already " + leaveRequest.getStatus()
                    + " and cannot be updated.");
        }

        // Only APPROVED or REJECTED are valid target statuses via this endpoint
        if (request.getStatus() == LeaveStatus.PENDING) {
            throw new BadRequestException(
                    "Target status must be APPROVED or REJECTED, not PENDING.");
        }

        leaveRequest.setStatus(request.getStatus());
        leaveRequest.setAdminRemarks(request.getAdminRemarks());

        LeaveRequest updated = leaveRequestRepository.save(leaveRequest);
        log.info("Leave {} : id={}, employeeId={}",
                updated.getStatus(), leaveId, updated.getEmployee().getEmployeeId());
        return toResponse(updated);
    }

    // -----------------------------------------------------------------------
    // View by ID
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public LeaveResponse getLeaveById(Long leaveId) {
        return toResponse(findLeaveOrThrow(leaveId));
    }

    // -----------------------------------------------------------------------
    // Leave History by Employee
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getLeaveHistoryByEmployee(Long employeeId, LeaveStatus status) {
        findEmployeeOrThrow(employeeId);    // validate employee exists

        List<LeaveRequest> records = (status == null)
                ? leaveRequestRepository.findByEmployee_EmployeeIdOrderByAppliedOnDesc(employeeId)
                : leaveRequestRepository
                        .findByEmployee_EmployeeIdAndStatusOrderByAppliedOnDesc(employeeId, status);

        return records.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // All Leave Requests (admin view)
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getAllLeaveRequests() {
        return leaveRequestRepository.findAllByOrderByAppliedOnDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Pending Queue (admin action queue)
    // -----------------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public List<LeaveResponse> getPendingLeaveRequests() {
        return leaveRequestRepository.findByStatusOrderByAppliedOnDesc(LeaveStatus.PENDING)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // -----------------------------------------------------------------------
    // Helpers — lookup
    // -----------------------------------------------------------------------

    private LeaveRequest findLeaveOrThrow(Long id) {
        return leaveRequestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "LeaveRequest", "leaveId", id));
    }

    private Employee findEmployeeOrThrow(Long id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Employee", "employeeId", id));
    }

    // -----------------------------------------------------------------------
    // Mapper
    // -----------------------------------------------------------------------

    private LeaveResponse toResponse(LeaveRequest lr) {
        return LeaveResponse.builder()
                .leaveId(lr.getLeaveId())
                .employeeId(lr.getEmployee().getEmployeeId())
                .employeeName(lr.getEmployee().getFirstName() + " " + lr.getEmployee().getLastName())
                .leaveType(lr.getLeaveType())
                .startDate(DateUtil.format(lr.getStartDate()))
                .endDate(DateUtil.format(lr.getEndDate()))
                .totalDays(lr.getTotalDays())
                .reason(lr.getReason())
                .status(lr.getStatus())
                .adminRemarks(lr.getAdminRemarks())
                .appliedOn(DateUtil.format(lr.getAppliedOn()))
                .build();
    }
}
