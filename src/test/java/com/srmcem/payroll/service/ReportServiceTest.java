package com.srmcem.payroll.service;

import com.srmcem.payroll.dto.*;
import com.srmcem.payroll.entity.Attendance;
import com.srmcem.payroll.entity.Employee;
import com.srmcem.payroll.enums.AttendanceStatus;
import com.srmcem.payroll.report.service.ReportServiceImpl;
import com.srmcem.payroll.repository.AttendanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportServiceImpl}.
 *
 * Covers: generateEmployeeReport, generateAttendanceReport, generateLeaveReport,
 *         generatePayrollReport, generateDepartmentReport.
 */
@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private EmployeeService employeeService;
    @Mock private AttendanceRepository attendanceRepository;
    @Mock private LeaveService leaveService;
    @Mock private PayrollService payrollService;
    @Mock private DepartmentService departmentService;
    @Mock private CompanySettingsService settingsService;

    @InjectMocks
    private ReportServiceImpl reportService;

    private CompanySettingsDto settings;

    @BeforeEach
    void setUp() {
        settings = CompanySettingsDto.builder()
                .companyName("SRMCEM")
                .build();
        when(settingsService.getSettings()).thenReturn(settings);
    }

    // -----------------------------------------------------------------------
    // generateEmployeeReport()
    // -----------------------------------------------------------------------

    /**
     * TC-REPORT-01: generateEmployeeReport() PDF format.
     */
    @Test
    @DisplayName("TC-REPORT-01: generateEmployeeReport() PDF - returns non-empty byte array")
    void generateEmployeeReport_pdf_returnsBytes() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        byte[] report = reportService.generateEmployeeReport("pdf");

        assertThat(report).isNotEmpty();
    }

    /**
     * TC-REPORT-02: generateEmployeeReport() Excel/xlsx format.
     */
    @Test
    @DisplayName("TC-REPORT-02: generateEmployeeReport() Excel - returns non-empty byte array")
    void generateEmployeeReport_excel_returnsBytes() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        byte[] report = reportService.generateEmployeeReport("excel");

        assertThat(report).isNotEmpty();
    }

    /**
     * TC-REPORT-03: generateEmployeeReport() CSV format.
     */
    @Test
    @DisplayName("TC-REPORT-03: generateEmployeeReport() CSV - returns non-empty byte array")
    void generateEmployeeReport_csv_returnsBytes() {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        byte[] report = reportService.generateEmployeeReport("csv");

        assertThat(report).isNotEmpty();
    }

    /**
     * TC-REPORT-04: generateEmployeeReport() unsupported format throws Exception.
     */
    @Test
    @DisplayName("TC-REPORT-04: generateEmployeeReport() unsupported format throws IllegalArgumentException")
    void generateEmployeeReport_unsupported_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> reportService.generateEmployeeReport("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unsupported format");
    }

    // -----------------------------------------------------------------------
    // generateAttendanceReport()
    // -----------------------------------------------------------------------

    /**
     * TC-REPORT-05: generateAttendanceReport() PDF format.
     */
    @Test
    @DisplayName("TC-REPORT-05: generateAttendanceReport() PDF - returns non-empty byte array")
    void generateAttendanceReport_pdf_returnsBytes() {
        Employee employee = Employee.builder().firstName("John").lastName("Doe").build();
        Attendance att = Attendance.builder().attendanceId(1L).employee(employee).date(LocalDate.now()).attendanceStatus(AttendanceStatus.PRESENT).build();

        when(attendanceRepository.findAll()).thenReturn(List.of(att));

        byte[] report = reportService.generateAttendanceReport("pdf");

        assertThat(report).isNotEmpty();
    }

    // -----------------------------------------------------------------------
    // generateLeaveReport()
    // -----------------------------------------------------------------------

    /**
     * TC-REPORT-06: generateLeaveReport() PDF format.
     */
    @Test
    @DisplayName("TC-REPORT-06: generateLeaveReport() PDF - returns non-empty byte array")
    void generateLeaveReport_pdf_returnsBytes() {
        when(leaveService.getAllLeaveRequests()).thenReturn(Collections.emptyList());

        byte[] report = reportService.generateLeaveReport("pdf");

        assertThat(report).isNotEmpty();
    }

    // -----------------------------------------------------------------------
    // generatePayrollReport()
    // -----------------------------------------------------------------------

    /**
     * TC-REPORT-07: generatePayrollReport() PDF format.
     */
    @Test
    @DisplayName("TC-REPORT-07: generatePayrollReport() PDF - returns non-empty byte array")
    void generatePayrollReport_pdf_returnsBytes() {
        when(payrollService.getAllPayrollRecords()).thenReturn(Collections.emptyList());

        byte[] report = reportService.generatePayrollReport("pdf");

        assertThat(report).isNotEmpty();
    }

    // -----------------------------------------------------------------------
    // generateDepartmentReport()
    // -----------------------------------------------------------------------

    /**
     * TC-REPORT-08: generateDepartmentReport() PDF format.
     */
    @Test
    @DisplayName("TC-REPORT-08: generateDepartmentReport() PDF - returns non-empty byte array")
    void generateDepartmentReport_pdf_returnsBytes() {
        when(departmentService.getAllDepartments()).thenReturn(Collections.emptyList());

        byte[] report = reportService.generateDepartmentReport("pdf");

        assertThat(report).isNotEmpty();
    }
}
