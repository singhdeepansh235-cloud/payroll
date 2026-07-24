package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.AttendanceRequest;
import com.srmcem.payroll.dto.AttendanceResponse;
import com.srmcem.payroll.dto.MonthlyAttendanceResponse;
import com.srmcem.payroll.entity.Attendance;
import com.srmcem.payroll.entity.Department;
import com.srmcem.payroll.entity.Designation;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.enums.AttendanceStatus;
import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import com.srmcem.payroll.exception.BadRequestException;
import com.srmcem.payroll.exception.ResourceNotFoundException;
import com.srmcem.payroll.repository.AttendanceRepository;
import com.srmcem.payroll.repository.EmployeeRepository;
import com.srmcem.payroll.service.impl.AttendanceServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link AttendanceServiceImpl}.
 *
 * Covers: markAttendance, updateAttendance, getAttendanceById,
 *         getAttendanceByEmployee, getAttendanceByDate, getMonthlyAttendance.
 */
@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {

    @Mock private AttendanceRepository attendanceRepository;
    @Mock private EmployeeRepository   employeeRepository;

    @InjectMocks
    private AttendanceServiceImpl attendanceService;

    private Employee   emp;
    private Attendance sampleAttendance;
    private AttendanceRequest request;

    @BeforeEach
    void setUp() {
        Department dept = Department.builder()
                .departmentId(1L).departmentName("Engineering").build();
        Designation desg = Designation.builder()
                .designationId(1L).designationName("Software Engineer").build();

        emp = Employee.builder()
                .employeeId(1L)
                .firstName("John").lastName("Doe")
                .gender(Gender.MALE)
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .email("john.doe@company.com")
                .phone("+919876543210")
                .department(dept).designation(desg)
                .joiningDate(LocalDate.of(2023, 1, 10))
                .salary(new BigDecimal("75000.00"))
                .status(EmployeeStatus.ACTIVE)
                .build();

        sampleAttendance = Attendance.builder()
                .attendanceId(1L)
                .employee(emp)
                .date(LocalDate.of(2026, 7, 24))
                .checkIn(LocalTime.of(9, 0))
                .checkOut(LocalTime.of(17, 30))
                .workingHours(8.5)
                .attendanceStatus(AttendanceStatus.PRESENT)
                .build();

        request = new AttendanceRequest();
        request.setEmployeeId(1L);
        request.setDate("2026-07-24");
        request.setCheckIn("09:00");
        request.setCheckOut("17:30");
        request.setAttendanceStatus(AttendanceStatus.PRESENT);
    }

    // -----------------------------------------------------------------------
    // markAttendance()
    // -----------------------------------------------------------------------

    /**
     * TC-ATT-01: markAttendance() — happy path creates a new attendance record.
     * Verifies the record is saved and the response has correct fields.
     */
    @Test
    @DisplayName("TC-ATT-01: markAttendance() — success marks attendance and returns response")
    void markAttendance_success_returnsResponse() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(attendanceRepository.existsByEmployee_EmployeeIdAndDate(1L, LocalDate.of(2026, 7, 24)))
                .thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(sampleAttendance);

        AttendanceResponse response = attendanceService.markAttendance(request);

        assertThat(response).isNotNull();
        assertThat(response.getAttendanceId()).isEqualTo(1L);
        assertThat(response.getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
        assertThat(response.getWorkingHours()).isEqualTo(8.5);

        verify(attendanceRepository).save(any(Attendance.class));
    }

    /**
     * TC-ATT-02: markAttendance() — employee not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-ATT-02: markAttendance() — employee not found throws ResourceNotFoundException")
    void markAttendance_employeeNotFound_throwsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        request.setEmployeeId(99L);

        assertThatThrownBy(() -> attendanceService.markAttendance(request))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(attendanceRepository, never()).save(any());
    }

    /**
     * TC-ATT-03: markAttendance() — duplicate attendance throws BadRequestException.
     * Ensures a second attendance record for the same employee on the same date is rejected.
     */
    @Test
    @DisplayName("TC-ATT-03: markAttendance() — duplicate throws BadRequestException")
    void markAttendance_alreadyMarked_throwsBadRequestException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(attendanceRepository.existsByEmployee_EmployeeIdAndDate(1L, LocalDate.of(2026, 7, 24)))
                .thenReturn(true);

        assertThatThrownBy(() -> attendanceService.markAttendance(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already marked");

        verify(attendanceRepository, never()).save(any());
    }

    /**
     * TC-ATT-04: markAttendance() — absent status with no checkIn/checkOut saves null times.
     * Verifies that ABSENT records don't require check-in/out times.
     */
    @Test
    @DisplayName("TC-ATT-04: markAttendance() — ABSENT status with no times saves null workingHours")
    void markAttendance_absentStatus_savesNullTimes() {
        request.setCheckIn(null);
        request.setCheckOut(null);
        request.setAttendanceStatus(AttendanceStatus.ABSENT);

        Attendance absentRecord = Attendance.builder()
                .attendanceId(2L).employee(emp)
                .date(LocalDate.of(2026, 7, 24))
                .checkIn(null).checkOut(null).workingHours(null)
                .attendanceStatus(AttendanceStatus.ABSENT)
                .build();

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(attendanceRepository.existsByEmployee_EmployeeIdAndDate(1L, LocalDate.of(2026, 7, 24)))
                .thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(absentRecord);

        AttendanceResponse response = attendanceService.markAttendance(request);

        assertThat(response.getAttendanceStatus()).isEqualTo(AttendanceStatus.ABSENT);
        assertThat(response.getCheckIn()).isNull();
        assertThat(response.getWorkingHours()).isNull();
    }

    // -----------------------------------------------------------------------
    // updateAttendance()
    // -----------------------------------------------------------------------

    /**
     * TC-ATT-05: updateAttendance() — happy path updates attendance and returns response.
     */
    @Test
    @DisplayName("TC-ATT-05: updateAttendance() — success updates record and returns response")
    void updateAttendance_success_returnsUpdatedResponse() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(sampleAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(sampleAttendance);

        AttendanceResponse response = attendanceService.updateAttendance(1L, request);

        assertThat(response).isNotNull();
        assertThat(response.getAttendanceId()).isEqualTo(1L);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    /**
     * TC-ATT-06: updateAttendance() — attendance record not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-ATT-06: updateAttendance() — not found throws ResourceNotFoundException")
    void updateAttendance_notFound_throwsResourceNotFoundException() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.updateAttendance(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getAttendanceById()
    // -----------------------------------------------------------------------

    /**
     * TC-ATT-07: getAttendanceById() — returns response for known attendance ID.
     */
    @Test
    @DisplayName("TC-ATT-07: getAttendanceById() — returns AttendanceResponse for existing record")
    void getAttendanceById_found_returnsResponse() {
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(sampleAttendance));

        AttendanceResponse response = attendanceService.getAttendanceById(1L);

        assertThat(response.getAttendanceId()).isEqualTo(1L);
        assertThat(response.getEmployeeName()).isEqualTo("John Doe");
    }

    /**
     * TC-ATT-08: getAttendanceById() — unknown ID throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-ATT-08: getAttendanceById() — not found throws ResourceNotFoundException")
    void getAttendanceById_notFound_throwsResourceNotFoundException() {
        when(attendanceRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.getAttendanceById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getAttendanceByEmployee()
    // -----------------------------------------------------------------------

    /**
     * TC-ATT-09: getAttendanceByEmployee() — returns history for a valid employee.
     */
    @Test
    @DisplayName("TC-ATT-09: getAttendanceByEmployee() — returns attendance history for employee")
    void getAttendanceByEmployee_found_returnsHistory() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(attendanceRepository.findByEmployee_EmployeeIdOrderByDateDesc(1L))
                .thenReturn(List.of(sampleAttendance));

        List<AttendanceResponse> responses = attendanceService.getAttendanceByEmployee(1L);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getAttendanceStatus()).isEqualTo(AttendanceStatus.PRESENT);
    }

    /**
     * TC-ATT-10: getAttendanceByEmployee() — employee not found throws ResourceNotFoundException.
     */
    @Test
    @DisplayName("TC-ATT-10: getAttendanceByEmployee() — employee not found throws ResourceNotFoundException")
    void getAttendanceByEmployee_employeeNotFound_throwsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attendanceService.getAttendanceByEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -----------------------------------------------------------------------
    // getAttendanceByDate()
    // -----------------------------------------------------------------------

    /**
     * TC-ATT-11: getAttendanceByDate() — with valid date returns records for that date.
     */
    @Test
    @DisplayName("TC-ATT-11: getAttendanceByDate() — with date string returns matching records")
    void getAttendanceByDate_withDate_returnsRecords() {
        when(attendanceRepository.findByDateOrderByEmployee_FirstNameAsc(LocalDate.of(2026, 7, 24)))
                .thenReturn(List.of(sampleAttendance));

        List<AttendanceResponse> responses = attendanceService.getAttendanceByDate("2026-07-24");

        assertThat(responses).hasSize(1);
    }

    /**
     * TC-ATT-12: getAttendanceByDate() — null date defaults to today.
     * Verifies the service uses LocalDate.now() when no date is provided.
     */
    @Test
    @DisplayName("TC-ATT-12: getAttendanceByDate() — null date defaults to today")
    void getAttendanceByDate_nullDate_defaultsToToday() {
        when(attendanceRepository.findByDateOrderByEmployee_FirstNameAsc(any(LocalDate.class)))
                .thenReturn(List.of());

        List<AttendanceResponse> responses = attendanceService.getAttendanceByDate(null);

        assertThat(responses).isEmpty();
        verify(attendanceRepository).findByDateOrderByEmployee_FirstNameAsc(any(LocalDate.class));
    }

    // -----------------------------------------------------------------------
    // getMonthlyAttendance()
    // -----------------------------------------------------------------------

    /**
     * TC-ATT-13: getMonthlyAttendance() — returns summary with correct employee info and period.
     */
    @Test
    @DisplayName("TC-ATT-13: getMonthlyAttendance() — returns monthly summary with correct period")
    void getMonthlyAttendance_valid_returnsSummary() {
        LocalDate start = LocalDate.of(2026, 7, 1);
        LocalDate end   = LocalDate.of(2026, 7, 31);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(attendanceRepository.findByEmployee_EmployeeIdAndDateBetweenOrderByDateAsc(1L, start, end))
                .thenReturn(List.of(sampleAttendance));
        when(attendanceRepository.countByStatusForEmployee(1L, start, end))
                .thenReturn(java.util.Collections.singletonList(new Object[]{AttendanceStatus.PRESENT, 1L}));

        MonthlyAttendanceResponse response = attendanceService.getMonthlyAttendance(1L, "July-2026");

        assertThat(response).isNotNull();
        assertThat(response.getEmployeeId()).isEqualTo(1L);
        assertThat(response.getEmployeeName()).isEqualTo("John Doe");
        assertThat(response.getPeriod()).isEqualTo("July-2026");
        assertThat(response.getStatusSummary()).containsKey("PRESENT");
        assertThat(response.getStatusSummary().get("PRESENT")).isEqualTo(1L);
    }
}
