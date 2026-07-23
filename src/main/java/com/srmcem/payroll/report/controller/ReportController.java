package com.srmcem.payroll.report.controller;

import com.srmcem.payroll.report.service.ReportService;
import com.srmcem.payroll.report.util.ReportUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * REST endpoints for the Reports module.
 *
 * <p>Base path: {@code /api/reports}
 */
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final com.srmcem.payroll.service.AuditLogService auditLogService;

    // -----------------------------------------------------------------------
    // GET /api/reports/employees/*
    // -----------------------------------------------------------------------

    @GetMapping("/employees/pdf")
    public ResponseEntity<byte[]> getEmployeeReportPdf() {
        return downloadReport("employees", "pdf", reportService.generateEmployeeReport("pdf"));
    }

    @GetMapping("/employees/excel")
    public ResponseEntity<byte[]> getEmployeeReportExcel() {
        return downloadReport("employees", "xlsx", reportService.generateEmployeeReport("excel"));
    }

    @GetMapping("/employees/csv")
    public ResponseEntity<byte[]> getEmployeeReportCsv() {
        return downloadReport("employees", "csv", reportService.generateEmployeeReport("csv"));
    }

    // -----------------------------------------------------------------------
    // GET /api/reports/attendance/pdf
    // -----------------------------------------------------------------------

    @GetMapping("/attendance/pdf")
    public ResponseEntity<byte[]> getAttendanceReportPdf() {
        return downloadReport("attendance", "pdf", reportService.generateAttendanceReport("pdf"));
    }

    // -----------------------------------------------------------------------
    // GET /api/reports/payroll/pdf
    // -----------------------------------------------------------------------

    @GetMapping("/payroll/pdf")
    public ResponseEntity<byte[]> getPayrollReportPdf() {
        return downloadReport("payroll", "pdf", reportService.generatePayrollReport("pdf"));
    }

    // -----------------------------------------------------------------------
    // GET /api/reports/leaves/pdf
    // -----------------------------------------------------------------------

    @GetMapping("/leaves/pdf")
    public ResponseEntity<byte[]> getLeaveReportPdf() {
        return downloadReport("leaves", "pdf", reportService.generateLeaveReport("pdf"));
    }

    // -----------------------------------------------------------------------
    // GET /api/reports/departments/pdf
    // -----------------------------------------------------------------------

    @GetMapping("/departments/pdf")
    public ResponseEntity<byte[]> getDepartmentReportPdf() {
        return downloadReport("departments", "pdf", reportService.generateDepartmentReport("pdf"));
    }

    // -----------------------------------------------------------------------
    // Private Helper
    // -----------------------------------------------------------------------

    private ResponseEntity<byte[]> downloadReport(String namePrefix, String extension, byte[] data) {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String filename = namePrefix + "_report_" + timestamp + "." + extension;

        auditLogService.log("Downloaded report: Name=" + namePrefix + ", Format=" + extension, "Reports");
        return ResponseEntity.ok()
                .headers(ReportUtil.createAttachmentHeaders(filename))
                .contentType(ReportUtil.getMediaTypeForFormat(extension))
                .body(data);
    }
}
