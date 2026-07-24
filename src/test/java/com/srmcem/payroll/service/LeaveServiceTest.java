package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.LeaveApplyRequest;
import com.srmcem.payroll.dto.LeaveResponse;
import com.srmcem.payroll.dto.LeaveStatusUpdateRequest;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.LeaveRequest;
import com.srmcem.payroll.enums.LeaveStatus;
import com.srmcem.payroll.enums.LeaveType;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.mail.MailService;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.repository.LeaveRequestRepository;
import com.srmcem.payroll.service.impl.LeaveServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link LeaveServiceImpl}.
 *
 * Covers: applyLeave, updateLeaveStatus, getLeaveById,
 *         getLeaveHistoryByEmployee, getAllLeaveRequests, getPendingLeaveRequests.
 */
@ExtendWith(MockitoExtension.class)
class LeaveServiceTest {

    @Mock private LeaveRequestRepository leaveRequestRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private MailService mailService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee employee;
    private LeaveRequest pendingLeave;
    private LeaveApplyRequest applyRequest;

    @BeforeEach
    void setUp() {
        employee = Employee.builder()
                .employeeId(1L)
                .firstName("Jane")
                .lastName("Doe")
                .email("jane.doe@company.com")
                .build();

        pendingLeave = LeaveRequest.builder()
                .leaveId(1L)
                .employee(employee)
                .leaveType(LeaveType.SICK)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 3))
                .totalDays(3)
                .reason("Flu")
                .status(LeaveStatus.PENDING)
                .appliedOn(LocalDate.now())
                .build();

        applyRequest = new LeaveApplyRequest();
        applyRequest.setEmployeeId(1L);
        applyRequest.setLeaveType(LeaveType.SICK);
        applyRequest.setStartDate("2026-08-01");
        applyRequest.setEndDate("2026-08-03");
        applyRequest.setReason("Flu");
    }

    // -----------------------------------------------------------------------
    // applyLeave()
    // -----------------------------------------------------------------------

    /**
     * TC-LEAVE-01: applyLeave() - successful request.
     * Verifies that the leave request is calculated, saved, and returned.
     */
    @Test
    @DisplayName("TC-LEAVE-01: applyLeave() - success applies leave request")
    void applyLeave_success_returnsLeaveResponse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.hasOverlappingLeave(1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)))
                .thenReturn(false);
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(pendingLeave);

        LeaveResponse response = leaveService.applyLeave(applyRequest);

        assertThat(response).isNotNull();
        assertThat(response.getLeaveId()).isEqualTo(1L);
        assertThat(response.getTotalDays()).isEqualTo(3);
        assertThat(response.getStatus()).isEqualTo(LeaveStatus.PENDING);
        verify(leaveRequestRepository).save(any(LeaveRequest.class));
    }

    /**
     * TC-LEAVE-02: applyLeave() - invalid date range (endDate < startDate) throws BadRequestException.
     */
    @Test
    @DisplayName("TC-LEAVE-02: applyLeave() - invalid dates range throws BadRequestException")
    void applyLeave_invalidDates_throwsBadRequestException() {
        applyRequest.setEndDate("2026-07-31"); // before 2026-08-01
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));

        assertThatThrownBy(() -> leaveService.applyLeave(applyRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be before");

        verify(leaveRequestRepository, never()).save(any());
    }

    /**
     * TC-LEAVE-03: applyLeave() - overlapping request throws BadRequestException.
     */
    @Test
    @DisplayName("TC-LEAVE-03: applyLeave() - overlapping leaves throws BadRequestException")
    void applyLeave_overlappingLeave_throwsBadRequestException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.hasOverlappingLeave(1L, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 3)))
                .thenReturn(true);

        assertThatThrownBy(() -> leaveService.applyLeave(applyRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("overlapping leave");

        verify(leaveRequestRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // updateLeaveStatus()
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("TC-LEAVE-04: updateLeaveStatus() - approve leave updates status and sends email")
    void updateLeaveStatus_approve_updatesStatusAndSendsEmail() {
        LeaveStatusUpdateRequest updateRequest = new LeaveStatusUpdateRequest();
        updateRequest.setStatus(LeaveStatus.APPROVED);
        updateRequest.setAdminRemarks("Approved remarks");

        LeaveRequest inputLeave = LeaveRequest.builder()
                .leaveId(1L).employee(employee).leaveType(LeaveType.SICK)
                .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                .totalDays(3).reason("Flu").status(LeaveStatus.PENDING).appliedOn(LocalDate.now()).build();

        LeaveRequest approvedLeave = LeaveRequest.builder()
                .leaveId(1L).employee(employee).leaveType(LeaveType.SICK)
                .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                .totalDays(3).reason("Flu").status(LeaveStatus.APPROVED)
                .adminRemarks("Approved remarks").appliedOn(LocalDate.now()).build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(inputLeave));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(approvedLeave);
        doNothing().when(mailService).sendLeaveApprovedEmail(any(LeaveRequest.class));
        doNothing().when(auditLogService).log(anyString(), anyString());

        LeaveResponse response = leaveService.updateLeaveStatus(1L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(LeaveStatus.APPROVED);
        assertThat(response.getAdminRemarks()).isEqualTo("Approved remarks");
        verify(mailService).sendLeaveApprovedEmail(any(LeaveRequest.class));
    }

    /**
     * TC-LEAVE-05: updateLeaveStatus() - reject request sends email.
     */
    @Test
    @DisplayName("TC-LEAVE-05: updateLeaveStatus() - reject leave updates status and sends email")
    void updateLeaveStatus_reject_updatesStatusAndSendsEmail() {
        LeaveStatusUpdateRequest updateRequest = new LeaveStatusUpdateRequest();
        updateRequest.setStatus(LeaveStatus.REJECTED);
        updateRequest.setAdminRemarks("Rejected remarks");

        LeaveRequest inputLeave = LeaveRequest.builder()
                .leaveId(1L).employee(employee).leaveType(LeaveType.SICK)
                .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                .totalDays(3).reason("Flu").status(LeaveStatus.PENDING).appliedOn(LocalDate.now()).build();

        LeaveRequest rejectedLeave = LeaveRequest.builder()
                .leaveId(1L).employee(employee).leaveType(LeaveType.SICK)
                .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                .totalDays(3).reason("Flu").status(LeaveStatus.REJECTED)
                .adminRemarks("Rejected remarks").appliedOn(LocalDate.now()).build();

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(inputLeave));
        when(leaveRequestRepository.save(any(LeaveRequest.class))).thenReturn(rejectedLeave);
        doNothing().when(mailService).sendLeaveRejectedEmail(any(LeaveRequest.class));
        doNothing().when(auditLogService).log(anyString(), anyString());

        LeaveResponse response = leaveService.updateLeaveStatus(1L, updateRequest);

        assertThat(response.getStatus()).isEqualTo(LeaveStatus.REJECTED);
        verify(mailService).sendLeaveRejectedEmail(any(LeaveRequest.class));
    }

    /**
     * TC-LEAVE-06: updateLeaveStatus() - non-pending request throws BadRequestException.
     */
    @Test
    @DisplayName("TC-LEAVE-06: updateLeaveStatus() - already actioned leave throws BadRequestException")
    void updateLeaveStatus_alreadyProcessed_throwsBadRequestException() {
        pendingLeave.setStatus(LeaveStatus.APPROVED); // Change mock behavior before when() call
        LeaveStatusUpdateRequest updateRequest = new LeaveStatusUpdateRequest();
        updateRequest.setStatus(LeaveStatus.REJECTED);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pendingLeave));

        assertThatThrownBy(() -> leaveService.updateLeaveStatus(1L, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already");

        verify(leaveRequestRepository, never()).save(any());
    }

    /**
     * TC-LEAVE-07: updateLeaveStatus() - setting status back to PENDING throws BadRequestException.
     */
    @Test
    @DisplayName("TC-LEAVE-07: updateLeaveStatus() - target status PENDING throws BadRequestException")
    void updateLeaveStatus_targetPending_throwsBadRequestException() {
        LeaveStatusUpdateRequest updateRequest = new LeaveStatusUpdateRequest();
        updateRequest.setStatus(LeaveStatus.PENDING);

        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pendingLeave));

        assertThatThrownBy(() -> leaveService.updateLeaveStatus(1L, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("must be APPROVED or REJECTED");

        verify(leaveRequestRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getLeaveById()
    // -----------------------------------------------------------------------

    /**
     * TC-LEAVE-08: getLeaveById() - found request.
     */
    @Test
    @DisplayName("TC-LEAVE-08: getLeaveById() - returns LeaveResponse for existing ID")
    void getLeaveById_found_returnsResponse() {
        when(leaveRequestRepository.findById(1L)).thenReturn(Optional.of(pendingLeave));

        LeaveResponse response = leaveService.getLeaveById(1L);

        assertThat(response.getLeaveId()).isEqualTo(1L);
        assertThat(response.getEmployeeName()).isEqualTo("Jane Doe");
    }

    /**
     * TC-LEAVE-09: getLeaveById() - not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-LEAVE-09: getLeaveById() - not found throws ResourceNotFoundException")
    void getLeaveById_notFound_throwsResourceNotFoundException() {
        when(leaveRequestRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> leaveService.getLeaveById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getLeaveHistoryByEmployee()
    // -----------------------------------------------------------------------

    /**
     * TC-LEAVE-10: getLeaveHistoryByEmployee() - returns employee's history.
     */
    @Test
    @DisplayName("TC-LEAVE-10: getLeaveHistoryByEmployee() - returns leaves history")
    void getLeaveHistoryByEmployee_returnsHistory() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(leaveRequestRepository.findByEmployee_EmployeeIdOrderByAppliedOnDesc(1L))
                .thenReturn(List.of(pendingLeave));

        List<LeaveResponse> list = leaveService.getLeaveHistoryByEmployee(1L, null);

        assertThat(list).hasSize(1);
    }

    // -----------------------------------------------------------------------
    // getAllLeaveRequests() & getPendingLeaveRequests()
    // -----------------------------------------------------------------------

    /**
     * TC-LEAVE-11: getAllLeaveRequests() - returns all requests.
     */
    @Test
    @DisplayName("TC-LEAVE-11: getAllLeaveRequests() - returns all leave requests")
    void getAllLeaveRequests_returnsList() {
        when(leaveRequestRepository.findAllByOrderByAppliedOnDesc()).thenReturn(List.of(pendingLeave));

        List<LeaveResponse> list = leaveService.getAllLeaveRequests();

        assertThat(list).hasSize(1);
    }

    /**
     * TC-LEAVE-12: getPendingLeaveRequests() - returns pending requests.
     */
    @Test
    @DisplayName("TC-LEAVE-12: getPendingLeaveRequests() - returns only pending leaves")
    void getPendingLeaveRequests_returnsPendingList() {
        when(leaveRequestRepository.findByStatusOrderByAppliedOnDesc(LeaveStatus.PENDING))
                .thenReturn(List.of(pendingLeave));

        List<LeaveResponse> list = leaveService.getPendingLeaveRequests();

        assertThat(list).hasSize(1);
    }
}
