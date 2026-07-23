package com.srmcem.payroll.leave;

import com.srmcem.payroll.dto.LeaveApplyRequest;
import com.srmcem.payroll.dto.LeaveResponse;
import com.srmcem.payroll.dto.LeaveStatusUpdateRequest;
import com.srmcem.payroll.entity.Department;
import com.srmcem.payroll.entity.Designation;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.entity.LeaveRequest;
import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import com.srmcem.payroll.enums.LeaveStatus;
import com.srmcem.payroll.enums.LeaveType;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.repository.LeaveRequestRepository;
import com.srmcem.payroll.service.impl.LeaveServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
class LeaveServiceImplTest {

    @Mock
    private LeaveRequestRepository leaveRequestRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private LeaveServiceImpl leaveService;

    private Employee employee;
    private LeaveRequest pendingLeave;

    @BeforeEach
    void setUpFixtures() {
        Department dept = Department.builder()
                .departmentId(1L)
                .departmentName("Engineering")
                .build();

        Designation desig = Designation.builder()
                .designationId(1L)
                .designationName("Software Engineer")
                .build();

        employee = Employee.builder()
                .employeeId(10L)
                .firstName("Alice")
                .lastName("Smith")
                .email("alice@example.com")
                .phone("9876543210")
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(1995, 5, 15))
                .joiningDate(LocalDate.of(2020, 1, 1))
                .salary(new BigDecimal("60000"))
                .status(EmployeeStatus.ACTIVE)
                .department(dept)
                .designation(desig)
                .build();

        pendingLeave = LeaveRequest.builder()
                .leaveId(100L)
                .employee(employee)
                .leaveType(LeaveType.CASUAL)
                .startDate(LocalDate.of(2026, 8, 1))
                .endDate(LocalDate.of(2026, 8, 3))
                .totalDays(3)
                .reason("Family function")
                .status(LeaveStatus.PENDING)
                .appliedOn(LocalDate.now())
                .build();
    }

    // Apply Leave tests

    @Nested
    @DisplayName("applyLeave()")
    class ApplyLeave {

        private LeaveApplyRequest validRequest() {
            LeaveApplyRequest req = new LeaveApplyRequest();
            req.setEmployeeId(10L);
            req.setLeaveType(LeaveType.CASUAL);
            req.setStartDate("2026-08-01");
            req.setEndDate("2026-08-03");
            req.setReason("Family function");
            return req;
        }

        @Test
        @DisplayName("saves and returns LeaveResponse when input is valid")
        void applyLeave_validInput_returnsResponse() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(leaveRequestRepository.hasOverlappingLeave(eq(10L), any(), any())).willReturn(false);
            given(leaveRequestRepository.save(any(LeaveRequest.class))).willReturn(pendingLeave);

            LeaveResponse response = leaveService.applyLeave(validRequest());

            assertThat(response).isNotNull();
            assertThat(response.getLeaveId()).isEqualTo(100L);
            assertThat(response.getEmployeeId()).isEqualTo(10L);
            assertThat(response.getEmployeeName()).isEqualTo("Alice Smith");
            assertThat(response.getLeaveType()).isEqualTo(LeaveType.CASUAL);
            assertThat(response.getStatus()).isEqualTo(LeaveStatus.PENDING);
            assertThat(response.getTotalDays()).isEqualTo(3);
            then(leaveRequestRepository).should().save(any(LeaveRequest.class));
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when employee does not exist")
        void applyLeave_unknownEmployee_throws404() {
            given(employeeRepository.findById(99L)).willReturn(Optional.empty());
            LeaveApplyRequest req = validRequest();
            req.setEmployeeId(99L);
            assertThatThrownBy(() -> leaveService.applyLeave(req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws BadRequestException when endDate is before startDate")
        void applyLeave_endBeforeStart_throws400() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            LeaveApplyRequest req = validRequest();
            req.setStartDate("2026-08-05");
            req.setEndDate("2026-08-01");
            assertThatThrownBy(() -> leaveService.applyLeave(req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("End date cannot be before start date");
        }

        @Test
        @DisplayName("throws BadRequestException on overlapping PENDING/APPROVED leave")
        void applyLeave_overlappingLeave_throws400() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(leaveRequestRepository.hasOverlappingLeave(eq(10L), any(), any())).willReturn(true);
            assertThatThrownBy(() -> leaveService.applyLeave(validRequest()))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("PENDING or APPROVED leave already exists");
        }

        @Test
        @DisplayName("single-day leave returns totalDays = 1")
        void applyLeave_singleDay_totalDaysIsOne() {
            LeaveApplyRequest req = validRequest();
            req.setStartDate("2026-08-01");
            req.setEndDate("2026-08-01");

            LeaveRequest oneDayLeave = LeaveRequest.builder()
                    .leaveId(102L).employee(employee).leaveType(LeaveType.SICK)
                    .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 1))
                    .totalDays(1).reason("Sick").status(LeaveStatus.PENDING)
                    .appliedOn(LocalDate.now()).build();

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(leaveRequestRepository.hasOverlappingLeave(any(), any(), any())).willReturn(false);
            given(leaveRequestRepository.save(any())).willReturn(oneDayLeave);

            LeaveResponse response = leaveService.applyLeave(req);
            assertThat(response.getTotalDays()).isEqualTo(1);
        }
    }

    // Approve Leave tests

    @Nested
    @DisplayName("updateLeaveStatus() - Approve")
    class ApproveLeave {

        @Test
        @DisplayName("transitions PENDING to APPROVED")
        void approveLeave_pendingLeave_transitionsToApproved() {
            LeaveRequest approved = LeaveRequest.builder()
                    .leaveId(100L).employee(employee).leaveType(LeaveType.CASUAL)
                    .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                    .totalDays(3).reason("Family function").status(LeaveStatus.APPROVED)
                    .adminRemarks("Approved as per policy").appliedOn(LocalDate.now()).build();

            given(leaveRequestRepository.findById(100L)).willReturn(Optional.of(pendingLeave));
            given(leaveRequestRepository.save(any())).willReturn(approved);

            LeaveStatusUpdateRequest req = new LeaveStatusUpdateRequest();
            req.setStatus(LeaveStatus.APPROVED);
            req.setAdminRemarks("Approved as per policy");

            LeaveResponse response = leaveService.updateLeaveStatus(100L, req);
            assertThat(response.getStatus()).isEqualTo(LeaveStatus.APPROVED);
            assertThat(response.getAdminRemarks()).isEqualTo("Approved as per policy");
        }

        @Test
        @DisplayName("throws ResourceNotFoundException when leaveId does not exist")
        void approveLeave_unknownLeaveId_throws404() {
            given(leaveRequestRepository.findById(999L)).willReturn(Optional.empty());
            LeaveStatusUpdateRequest req = new LeaveStatusUpdateRequest();
            req.setStatus(LeaveStatus.APPROVED);
            assertThatThrownBy(() -> leaveService.updateLeaveStatus(999L, req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("throws BadRequestException when leave is already APPROVED")
        void approveLeave_alreadyApproved_throws400() {
            LeaveRequest alreadyApproved = LeaveRequest.builder()
                    .leaveId(100L).employee(employee).leaveType(LeaveType.CASUAL)
                    .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                    .totalDays(3).status(LeaveStatus.APPROVED).appliedOn(LocalDate.now()).build();

            given(leaveRequestRepository.findById(100L)).willReturn(Optional.of(alreadyApproved));
            LeaveStatusUpdateRequest req = new LeaveStatusUpdateRequest();
            req.setStatus(LeaveStatus.APPROVED);

            assertThatThrownBy(() -> leaveService.updateLeaveStatus(100L, req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already APPROVED");
        }

        @Test
        @DisplayName("throws BadRequestException when target status is PENDING")
        void approveLeave_targetStatusPending_throws400() {
            given(leaveRequestRepository.findById(100L)).willReturn(Optional.of(pendingLeave));
            LeaveStatusUpdateRequest req = new LeaveStatusUpdateRequest();
            req.setStatus(LeaveStatus.PENDING);

            assertThatThrownBy(() -> leaveService.updateLeaveStatus(100L, req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("APPROVED or REJECTED");
        }
    }

    // Reject Leave tests

    @Nested
    @DisplayName("updateLeaveStatus() - Reject")
    class RejectLeave {

        @Test
        @DisplayName("transitions PENDING to REJECTED")
        void rejectLeave_pendingLeave_transitionsToRejected() {
            LeaveRequest rejected = LeaveRequest.builder()
                    .leaveId(100L).employee(employee).leaveType(LeaveType.CASUAL)
                    .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                    .totalDays(3).reason("Family function").status(LeaveStatus.REJECTED)
                    .adminRemarks("Insufficient staffing").appliedOn(LocalDate.now()).build();

            given(leaveRequestRepository.findById(100L)).willReturn(Optional.of(pendingLeave));
            given(leaveRequestRepository.save(any())).willReturn(rejected);

            LeaveStatusUpdateRequest req = new LeaveStatusUpdateRequest();
            req.setStatus(LeaveStatus.REJECTED);
            req.setAdminRemarks("Insufficient staffing");

            LeaveResponse response = leaveService.updateLeaveStatus(100L, req);
            assertThat(response.getStatus()).isEqualTo(LeaveStatus.REJECTED);
            assertThat(response.getAdminRemarks()).isEqualTo("Insufficient staffing");
        }

        @Test
        @DisplayName("throws BadRequestException when leave is already REJECTED")
        void rejectLeave_alreadyRejected_throws400() {
            LeaveRequest alreadyRejected = LeaveRequest.builder()
                    .leaveId(100L).employee(employee).leaveType(LeaveType.CASUAL)
                    .startDate(LocalDate.of(2026, 8, 1)).endDate(LocalDate.of(2026, 8, 3))
                    .totalDays(3).status(LeaveStatus.REJECTED).appliedOn(LocalDate.now()).build();

            given(leaveRequestRepository.findById(100L)).willReturn(Optional.of(alreadyRejected));
            LeaveStatusUpdateRequest req = new LeaveStatusUpdateRequest();
            req.setStatus(LeaveStatus.REJECTED);

            assertThatThrownBy(() -> leaveService.updateLeaveStatus(100L, req))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("already REJECTED");
        }
    }

    // Leave History tests

    @Nested
    @DisplayName("getLeaveHistoryByEmployee()")
    class ViewLeaveHistory {

        @Test
        @DisplayName("returns all leaves when status filter is null")
        void getHistory_noFilter_returnsAll() {
            LeaveRequest approvedLeave = LeaveRequest.builder()
                    .leaveId(101L).employee(employee).leaveType(LeaveType.SICK)
                    .startDate(LocalDate.of(2026, 7, 10)).endDate(LocalDate.of(2026, 7, 11))
                    .totalDays(2).status(LeaveStatus.APPROVED).appliedOn(LocalDate.now()).build();

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(leaveRequestRepository.findByEmployee_EmployeeIdOrderByAppliedOnDesc(10L))
                    .willReturn(List.of(pendingLeave, approvedLeave));

            List<LeaveResponse> history = leaveService.getLeaveHistoryByEmployee(10L, null);
            assertThat(history).hasSize(2);
            assertThat(history).extracting(LeaveResponse::getLeaveId).containsExactly(100L, 101L);
        }

        @Test
        @DisplayName("returns only PENDING leaves when status filter is PENDING")
        void getHistory_pendingFilter_returnsOnlyPending() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(leaveRequestRepository.findByEmployee_EmployeeIdAndStatusOrderByAppliedOnDesc(10L, LeaveStatus.PENDING))
                    .willReturn(List.of(pendingLeave));

            List<LeaveResponse> history = leaveService.getLeaveHistoryByEmployee(10L, LeaveStatus.PENDING);
            assertThat(history).hasSize(1);
            assertThat(history.get(0).getStatus()).isEqualTo(LeaveStatus.PENDING);
        }

        @Test
        @DisplayName("returns empty list when employee has no leaves")
        void getHistory_noLeaves_returnsEmptyList() {
            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(leaveRequestRepository.findByEmployee_EmployeeIdOrderByAppliedOnDesc(10L))
                    .willReturn(List.of());

            List<LeaveResponse> history = leaveService.getLeaveHistoryByEmployee(10L, null);
            assertThat(history).isEmpty();
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown employeeId")
        void getHistory_unknownEmployee_throws404() {
            given(employeeRepository.findById(99L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> leaveService.getLeaveHistoryByEmployee(99L, null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("returns only APPROVED leaves when status filter is APPROVED")
        void getHistory_approvedFilter_returnsOnlyApproved() {
            LeaveRequest approvedLeave = LeaveRequest.builder()
                    .leaveId(200L).employee(employee).leaveType(LeaveType.EARNED)
                    .startDate(LocalDate.of(2026, 9, 1)).endDate(LocalDate.of(2026, 9, 5))
                    .totalDays(5).status(LeaveStatus.APPROVED).appliedOn(LocalDate.now()).build();

            given(employeeRepository.findById(10L)).willReturn(Optional.of(employee));
            given(leaveRequestRepository.findByEmployee_EmployeeIdAndStatusOrderByAppliedOnDesc(10L, LeaveStatus.APPROVED))
                    .willReturn(List.of(approvedLeave));

            List<LeaveResponse> history = leaveService.getLeaveHistoryByEmployee(10L, LeaveStatus.APPROVED);
            assertThat(history).hasSize(1);
            assertThat(history.get(0).getStatus()).isEqualTo(LeaveStatus.APPROVED);
            assertThat(history.get(0).getLeaveId()).isEqualTo(200L);
        }
    }

    // getLeaveById tests

    @Nested
    @DisplayName("getLeaveById()")
    class GetLeaveById {

        @Test
        @DisplayName("returns LeaveResponse for a valid leaveId")
        void getLeaveById_validId_returnsResponse() {
            given(leaveRequestRepository.findById(100L)).willReturn(Optional.of(pendingLeave));
            LeaveResponse response = leaveService.getLeaveById(100L);
            assertThat(response.getLeaveId()).isEqualTo(100L);
            assertThat(response.getStatus()).isEqualTo(LeaveStatus.PENDING);
        }

        @Test
        @DisplayName("throws ResourceNotFoundException for unknown leaveId")
        void getLeaveById_unknownId_throws404() {
            given(leaveRequestRepository.findById(999L)).willReturn(Optional.empty());
            assertThatThrownBy(() -> leaveService.getLeaveById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // Admin view tests

    @Nested
    @DisplayName("getAllLeaveRequests() / getPendingLeaveRequests()")
    class AdminViews {

        @Test
        @DisplayName("getAllLeaveRequests returns all requests ordered newest-first")
        void getAllLeaveRequests_returnsAll() {
            given(leaveRequestRepository.findAllByOrderByAppliedOnDesc())
                    .willReturn(List.of(pendingLeave));
            List<LeaveResponse> all = leaveService.getAllLeaveRequests();
            assertThat(all).hasSize(1);
            assertThat(all.get(0).getLeaveId()).isEqualTo(100L);
        }

        @Test
        @DisplayName("getPendingLeaveRequests returns only PENDING entries")
        void getPendingLeaveRequests_returnsPending() {
            given(leaveRequestRepository.findByStatusOrderByAppliedOnDesc(LeaveStatus.PENDING))
                    .willReturn(List.of(pendingLeave));
            List<LeaveResponse> pending = leaveService.getPendingLeaveRequests();
            assertThat(pending).hasSize(1);
            assertThat(pending.get(0).getStatus()).isEqualTo(LeaveStatus.PENDING);
        }
    }
}