package com.srmcem.payroll.report.controller;

import com.srmcem.payroll.report.service.ReportService;
import com.srmcem.payroll.service.AuditLogService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration/Slice tests for {@link ReportController} using MockMvc.
 */
@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private AuditLogService auditLogService;

    @Test
    @DisplayName("GET /api/reports/employees/pdf - Success")
    void getEmployeeReportPdf_success() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        when(reportService.generateEmployeeReport("pdf")).thenReturn(data);
        doNothing().when(auditLogService).log(anyString(), anyString());

        mockMvc.perform(get("/api/reports/employees/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(data));
    }

    @Test
    @DisplayName("GET /api/reports/employees/excel - Success")
    void getEmployeeReportExcel_success() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        when(reportService.generateEmployeeReport("excel")).thenReturn(data);
        doNothing().when(auditLogService).log(anyString(), anyString());

        mockMvc.perform(get("/api/reports/employees/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")))
                .andExpect(content().bytes(data));
    }

    @Test
    @DisplayName("GET /api/reports/employees/csv - Success")
    void getEmployeeReportCsv_success() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        when(reportService.generateEmployeeReport("csv")).thenReturn(data);
        doNothing().when(auditLogService).log(anyString(), anyString());

        mockMvc.perform(get("/api/reports/employees/csv"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType("text/csv")))
                .andExpect(content().bytes(data));
    }

    @Test
    @DisplayName("GET /api/reports/attendance/pdf - Success")
    void getAttendanceReportPdf_success() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        when(reportService.generateAttendanceReport("pdf")).thenReturn(data);
        doNothing().when(auditLogService).log(anyString(), anyString());

        mockMvc.perform(get("/api/reports/attendance/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(data));
    }

    @Test
    @DisplayName("GET /api/reports/payroll/pdf - Success")
    void getPayrollReportPdf_success() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        when(reportService.generatePayrollReport("pdf")).thenReturn(data);
        doNothing().when(auditLogService).log(anyString(), anyString());

        mockMvc.perform(get("/api/reports/payroll/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(data));
    }

    @Test
    @DisplayName("GET /api/reports/leaves/pdf - Success")
    void getLeaveReportPdf_success() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        when(reportService.generateLeaveReport("pdf")).thenReturn(data);
        doNothing().when(auditLogService).log(anyString(), anyString());

        mockMvc.perform(get("/api/reports/leaves/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(data));
    }

    @Test
    @DisplayName("GET /api/reports/departments/pdf - Success")
    void getDepartmentReportPdf_success() throws Exception {
        byte[] data = new byte[]{1, 2, 3};
        when(reportService.generateDepartmentReport("pdf")).thenReturn(data);
        doNothing().when(auditLogService).log(anyString(), anyString());

        mockMvc.perform(get("/api/reports/departments/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(content().bytes(data));
    }
}
