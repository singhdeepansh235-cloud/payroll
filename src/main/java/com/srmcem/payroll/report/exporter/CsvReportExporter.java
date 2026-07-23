package com.srmcem.payroll.report.exporter;

import com.srmcem.payroll.dto.EmployeeResponse;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class CsvReportExporter {

    public static byte[] exportEmployees(List<EmployeeResponse> employees) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8)) {
            // Write BOM for Excel compatibility
            out.write(239);
            out.write(187);
            out.write(191);

            // Header
            writer.println("Employee ID,First Name,Last Name,Email,Phone,Department,Designation,Status");

            // Data
            for (EmployeeResponse emp : employees) {
                writer.printf("%d,%s,%s,%s,%s,%s,%s,%s%n",
                        emp.getEmployeeId(),
                        escape(emp.getFirstName()),
                        escape(emp.getLastName()),
                        escape(emp.getEmail()),
                        escape(emp.getPhone()),
                        escape(emp.getDepartmentName()),
                        escape(emp.getDesignationName()),
                        emp.getStatus()
                );
            }
        }
        return out.toByteArray();
    }

    private static String escape(String data) {
        if (data == null) {
            return "";
        }
        String escapedData = data.replaceAll("\\R", " ");
        if (data.contains(",") || data.contains("\"") || data.contains("'")) {
            data = data.replace("\"", "\"\"");
            escapedData = "\"" + data + "\"";
        }
        return escapedData;
    }
}
