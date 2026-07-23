package com.srmcem.payroll.report.exporter;

import com.srmcem.payroll.dto.EmployeeResponse;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

public class ExcelReportExporter {

    public static byte[] exportEmployees(List<EmployeeResponse> employees, String companyName) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Employees");

            // Title Row
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue(companyName + " - Employee Report");
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleCell.setCellStyle(titleStyle);

            // Header Row
            Row headerRow = sheet.createRow(2);
            String[] headers = {"Employee ID", "First Name", "Last Name", "Email", "Phone", "Department", "Designation", "Status"};
            
            CellStyle headerStyle = workbook.createCellStyle();
            Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 3;
            for (EmployeeResponse emp : employees) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(emp.getEmployeeId());
                row.createCell(1).setCellValue(emp.getFirstName());
                row.createCell(2).setCellValue(emp.getLastName());
                row.createCell(3).setCellValue(emp.getEmail());
                row.createCell(4).setCellValue(emp.getPhone());
                row.createCell(5).setCellValue(emp.getDepartmentName() != null ? emp.getDepartmentName() : "");
                row.createCell(6).setCellValue(emp.getDesignationName() != null ? emp.getDesignationName() : "");
                row.createCell(7).setCellValue(emp.getStatus().name());
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to generate Excel report", e);
        }
    }
}
