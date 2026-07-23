package com.srmcem.payroll.report.service;

public interface ReportService {
    byte[] generateEmployeeReport(String format);
    byte[] generateAttendanceReport(String format);
    byte[] generateLeaveReport(String format);
    byte[] generatePayrollReport(String format);
    byte[] generateDepartmentReport(String format);
}
