package com.srmcem.payroll.report.service;

import com.srmcem.payroll.dto.*;
import com.srmcem.payroll.entity.Attendance;
import com.srmcem.payroll.report.exporter.CsvReportExporter;
import com.srmcem.payroll.report.exporter.ExcelReportExporter;
import com.srmcem.payroll.report.exporter.PdfReportExporter;
import com.srmcem.payroll.repository.AttendanceRepository;
import com.srmcem.payroll.service.DepartmentService;
import com.srmcem.payroll.service.EmployeeService;
import com.srmcem.payroll.service.LeaveService;
import com.srmcem.payroll.service.PayrollService;
import com.srmcem.payroll.util.DateUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final EmployeeService employeeService;
    private final AttendanceRepository attendanceRepository;
    private final LeaveService leaveService;
    private final PayrollService payrollService;
    private final DepartmentService departmentService;
    private final com.srmcem.payroll.service.CompanySettingsService settingsService;

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    @Override
    public byte[] generateEmployeeReport(String format) {
        String companyName = getCompanyName();
        List<EmployeeResponse> employees = employeeService.getAllEmployees();
        return switch (format.toLowerCase()) {
            case "pdf" -> PdfReportExporter.exportEmployees(employees, companyName);
            case "excel", "xlsx" -> ExcelReportExporter.exportEmployees(employees, companyName);
            case "csv" -> CsvReportExporter.exportEmployees(employees); // CSV doesn't use title
            default -> throw new IllegalArgumentException("Unsupported format: " + format);
        };
    }

    @Override
    public byte[] generateAttendanceReport(String format) {
        List<Attendance> attendances = attendanceRepository.findAll();
        List<AttendanceResponse> responses = attendances.stream().map(a -> AttendanceResponse.builder()
                .attendanceId(a.getAttendanceId())
                .employeeId(a.getEmployee().getEmployeeId())
                .employeeName(a.getEmployee().getFirstName() + " " + a.getEmployee().getLastName())
                .date(DateUtil.format(a.getDate()))
                .checkIn(a.getCheckIn() != null ? a.getCheckIn().format(TIME_FMT) : null)
                .checkOut(a.getCheckOut() != null ? a.getCheckOut().format(TIME_FMT) : null)
                .workingHours(a.getWorkingHours())
                .attendanceStatus(a.getAttendanceStatus())
                .build()).collect(Collectors.toList());

        if ("pdf".equalsIgnoreCase(format)) {
            return PdfReportExporter.exportAttendance(responses, getCompanyName());
        }
        throw new IllegalArgumentException("Unsupported format for Attendance report: " + format);
    }

    @Override
    public byte[] generateLeaveReport(String format) {
        List<LeaveResponse> leaves = leaveService.getAllLeaveRequests();
        if ("pdf".equalsIgnoreCase(format)) {
            return PdfReportExporter.exportLeaves(leaves, getCompanyName());
        }
        throw new IllegalArgumentException("Unsupported format for Leave report: " + format);
    }

    @Override
    public byte[] generatePayrollReport(String format) {
        List<PayrollResponse> records = payrollService.getAllPayrollRecords();
        if ("pdf".equalsIgnoreCase(format)) {
            return PdfReportExporter.exportPayroll(records, getCompanyName());
        }
        throw new IllegalArgumentException("Unsupported format for Payroll report: " + format);
    }

    @Override
    public byte[] generateDepartmentReport(String format) {
        List<DepartmentResponse> departments = departmentService.getAllDepartments();
        if ("pdf".equalsIgnoreCase(format)) {
            return PdfReportExporter.exportDepartments(departments, getCompanyName());
        }
        throw new IllegalArgumentException("Unsupported format for Department report: " + format);
    }

    private String getCompanyName() {
        String companyName = settingsService.getSettings().getCompanyName();
        return (companyName == null || companyName.isEmpty()) ? "SRMCEM" : companyName;
    }
}
