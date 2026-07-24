package com.srmcem.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmcem.payroll.dto.PayrollGenerateRequest;
import com.srmcem.payroll.dto.PayrollResponse;
import com.srmcem.payroll.service.PayrollService;
import com.srmcem.payroll.service.PayslipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration/Slice tests for {@link PayrollController} using MockMvc.
 */
@WebMvcTest(PayrollController.class)
@AutoConfigureMockMvc(addFilters = false)
class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PayrollService payrollService;

    @MockitoBean
    private PayslipService payslipService;

    private PayrollGenerateRequest generateRequest;
    private PayrollResponse response;

    @BeforeEach
    void setUp() {
        generateRequest = new PayrollGenerateRequest();
        generateRequest.setEmployeeId(1L);
        generateRequest.setPayrollMonth("July-2026");
        generateRequest.setBasicSalary(new BigDecimal("60000.00"));
        generateRequest.setBonus(new BigDecimal("5000.00"));
        generateRequest.setOvertime(new BigDecimal("2000.00"));
        generateRequest.setDeductions(new BigDecimal("3000.00"));

        response = PayrollResponse.builder()
                .payrollId(1L)
                .employeeId(1L)
                .employeeName("Jane Doe")
                .payrollMonth("July-2026")
                .basicSalary(new BigDecimal("60000.00"))
                .bonus(new BigDecimal("5000.00"))
                .overtime(new BigDecimal("2000.00"))
                .deductions(new BigDecimal("3000.00"))
                .grossSalary(new BigDecimal("67000.00"))
                .netSalary(new BigDecimal("64000.00"))
                .build();
    }

    @Test
    @DisplayName("POST /api/payroll - Success")
    void generatePayroll_success() throws Exception {
        when(payrollService.generatePayroll(any(PayrollGenerateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(generateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.payrollId").value(1L));
    }

    @Test
    @DisplayName("GET /api/payroll/{id} - Success")
    void getPayrollById_success() throws Exception {
        when(payrollService.getPayrollById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/payroll/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.payrollId").value(1L));
    }

    @Test
    @DisplayName("GET /api/payroll/employee/{employeeId} - Success")
    void getPayrollHistoryByEmployee_success() throws Exception {
        when(payrollService.getPayrollHistoryByEmployee(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payroll/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].payrollId").value(1L));
    }

    @Test
    @DisplayName("GET /api/payroll/month - Success")
    void getPayrollByMonth_success() throws Exception {
        when(payrollService.getPayrollByMonth("July-2026")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payroll/month").param("period", "July-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].payrollId").value(1L));
    }

    @Test
    @DisplayName("GET /api/payroll - Success")
    void getAllPayrollRecords_success() throws Exception {
        when(payrollService.getAllPayrollRecords()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/payroll"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].payrollId").value(1L));
    }

    @Test
    @DisplayName("GET /api/payroll/{id}/payslip - Success")
    void downloadPayslip_success() throws Exception {
        byte[] pdfBytes = new byte[]{1, 2, 3};
        when(payslipService.generatePayslip(1L)).thenReturn(pdfBytes);

        mockMvc.perform(get("/api/payroll/1/payslip"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", "attachment; filename=\"payslip-1.pdf\""))
                .andExpect(content().bytes(pdfBytes));
    }
}
