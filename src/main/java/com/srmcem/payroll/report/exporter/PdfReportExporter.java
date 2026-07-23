package com.srmcem.payroll.report.exporter;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.srmcem.payroll.dto.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PdfReportExporter {

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font DATA_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final String GENERATED_FORMAT = "dd-MM-yyyy HH:mm:ss";

    public static byte[] exportEmployees(List<EmployeeResponse> employees) {
        return generatePdf("Employee Report", new String[]{"ID", "First Name", "Last Name", "Email", "Department", "Role", "Status"},
                employees, (emp, table) -> {
                    addCell(table, String.valueOf(emp.getEmployeeId()));
                    addCell(table, emp.getFirstName());
                    addCell(table, emp.getLastName());
                    addCell(table, emp.getEmail());
                    addCell(table, emp.getDepartmentName() != null ? emp.getDepartmentName() : "N/A");
                    addCell(table, emp.getDesignationName() != null ? emp.getDesignationName() : "N/A");
                    addCell(table, emp.getStatus().name());
                });
    }

    public static byte[] exportAttendance(List<AttendanceResponse> attendanceList) {
        return generatePdf("Attendance Report", new String[]{"ID", "Employee", "Date", "Check-in", "Check-out", "Hours", "Status"},
                attendanceList, (att, table) -> {
                    addCell(table, String.valueOf(att.getAttendanceId()));
                    addCell(table, att.getEmployeeName());
                    addCell(table, att.getDate());
                    addCell(table, att.getCheckIn() != null ? att.getCheckIn() : "-");
                    addCell(table, att.getCheckOut() != null ? att.getCheckOut() : "-");
                    addCell(table, att.getWorkingHours() != null ? String.valueOf(att.getWorkingHours()) : "-");
                    addCell(table, att.getAttendanceStatus().name());
                });
    }

    public static byte[] exportLeaves(List<LeaveResponse> leaves) {
        return generatePdf("Leave Requests Report", new String[]{"ID", "Employee", "Type", "Start", "End", "Days", "Status"},
                leaves, (leave, table) -> {
                    addCell(table, String.valueOf(leave.getLeaveId()));
                    addCell(table, leave.getEmployeeName());
                    addCell(table, leave.getLeaveType().name());
                    addCell(table, leave.getStartDate());
                    addCell(table, leave.getEndDate());
                    addCell(table, String.valueOf(leave.getTotalDays()));
                    addCell(table, leave.getStatus().name());
                });
    }

    public static byte[] exportPayroll(List<PayrollResponse> payrollRecords) {
        return generatePdf("Payroll Report", new String[]{"ID", "Employee", "Month", "Basic", "Gross", "Net"},
                payrollRecords, (payroll, table) -> {
                    addCell(table, String.valueOf(payroll.getPayrollId()));
                    addCell(table, payroll.getEmployeeName());
                    addCell(table, payroll.getPayrollMonth());
                    addCell(table, payroll.getBasicSalary().toString());
                    addCell(table, payroll.getGrossSalary().toString());
                    addCell(table, payroll.getNetSalary().toString());
                });
    }

    public static byte[] exportDepartments(List<DepartmentResponse> departments) {
        return generatePdf("Departments Report", new String[]{"ID", "Department Name", "Description"},
                departments, (dept, table) -> {
                    addCell(table, String.valueOf(dept.getDepartmentId()));
                    addCell(table, dept.getDepartmentName());
                    addCell(table, dept.getDescription() != null ? dept.getDescription() : "");
                });
    }

    // --- Private Helper Methods ---

    private interface DataRowMapper<T> {
        void mapRow(T item, PdfPTable table);
    }

    private static <T> byte[] generatePdf(String title, String[] headers, List<T> data, DataRowMapper<T> rowMapper) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate()); // Landscape for wide tables
            PdfWriter.getInstance(document, out);
            document.open();

            // Title
            Paragraph titlePara = new Paragraph("SRMCEM Payroll System - " + title, TITLE_FONT);
            titlePara.setAlignment(Element.ALIGN_CENTER);
            titlePara.setSpacingAfter(10);
            document.add(titlePara);

            // Generated date
            String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern(GENERATED_FORMAT));
            Paragraph datePara = new Paragraph("Generated on: " + dateStr, DATA_FONT);
            datePara.setAlignment(Element.ALIGN_RIGHT);
            datePara.setSpacingAfter(20);
            document.add(datePara);

            // Table
            PdfPTable table = new PdfPTable(headers.length);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10);

            // Headers
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(Color.LIGHT_GRAY);
                cell.setPadding(5);
                table.addCell(cell);
            }

            // Data
            for (T item : data) {
                rowMapper.mapRow(item, table);
            }

            document.add(table);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate PDF report", e);
        }
    }

    private static void addCell(PdfPTable table, String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, DATA_FONT));
        cell.setPadding(4);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }
}
