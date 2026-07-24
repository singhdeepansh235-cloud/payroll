package com.srmcem.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmcem.payroll.dto.LeaveApplyRequest;
import com.srmcem.payroll.dto.LeaveResponse;
import com.srmcem.payroll.dto.LeaveStatusUpdateRequest;
import com.srmcem.payroll.enums.LeaveStatus;
import com.srmcem.payroll.enums.LeaveType;
import com.srmcem.payroll.service.LeaveService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration/Slice tests for {@link LeaveController} using MockMvc.
 */
@WebMvcTest(LeaveController.class)
@AutoConfigureMockMvc(addFilters = false)
class LeaveControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LeaveService leaveService;

    private LeaveApplyRequest applyRequest;
    private LeaveResponse response;

    @BeforeEach
    void setUp() {
        applyRequest = new LeaveApplyRequest();
        applyRequest.setEmployeeId(1L);
        applyRequest.setLeaveType(LeaveType.SICK);
        applyRequest.setStartDate("2026-08-01");
        applyRequest.setEndDate("2026-08-03");
        applyRequest.setReason("Flu");

        response = LeaveResponse.builder()
                .leaveId(1L)
                .employeeId(1L)
                .employeeName("Jane Doe")
                .leaveType(LeaveType.SICK)
                .startDate("2026-08-01")
                .endDate("2026-08-03")
                .totalDays(3)
                .reason("Flu")
                .status(LeaveStatus.PENDING)
                .appliedOn("2026-07-24")
                .build();
    }

    @Test
    @DisplayName("POST /api/leaves - Success")
    void applyLeave_success() throws Exception {
        when(leaveService.applyLeave(any(LeaveApplyRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/leaves")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(applyRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leaveId").value(1L));
    }

    @Test
    @DisplayName("PATCH /api/leaves/{id}/status - Success")
    void updateLeaveStatus_success() throws Exception {
        LeaveStatusUpdateRequest updateRequest = new LeaveStatusUpdateRequest();
        updateRequest.setStatus(LeaveStatus.APPROVED);
        updateRequest.setAdminRemarks("Have a good rest");

        LeaveResponse approvedResponse = response;
        approvedResponse.setStatus(LeaveStatus.APPROVED);
        approvedResponse.setAdminRemarks("Have a good rest");

        when(leaveService.updateLeaveStatus(eq(1L), any(LeaveStatusUpdateRequest.class))).thenReturn(approvedResponse);

        mockMvc.perform(patch("/api/leaves/1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("APPROVED"));
    }

    @Test
    @DisplayName("GET /api/leaves/{id} - Success")
    void getLeaveById_success() throws Exception {
        when(leaveService.getLeaveById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/leaves/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.leaveId").value(1L));
    }

    @Test
    @DisplayName("GET /api/leaves/employee/{employeeId} - Success")
    void getLeaveHistoryByEmployee_success() throws Exception {
        when(leaveService.getLeaveHistoryByEmployee(1L, null)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/leaves/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].leaveId").value(1L));
    }

    @Test
    @DisplayName("GET /api/leaves - Success")
    void getAllLeaveRequests_success() throws Exception {
        when(leaveService.getAllLeaveRequests()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/leaves"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].leaveId").value(1L));
    }

    @Test
    @DisplayName("GET /api/leaves/pending - Success")
    void getPendingLeaveRequests_success() throws Exception {
        when(leaveService.getPendingLeaveRequests()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/leaves/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].leaveId").value(1L));
    }
}
