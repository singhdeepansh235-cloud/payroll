package com.srmcem.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmcem.payroll.dto.AttendanceRequest;
import com.srmcem.payroll.dto.AttendanceResponse;
import com.srmcem.payroll.dto.MonthlyAttendanceResponse;
import com.srmcem.payroll.enums.AttendanceStatus;
import com.srmcem.payroll.service.AttendanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration/Slice tests for {@link AttendanceController} using MockMvc.
 */
@WebMvcTest(AttendanceController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttendanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttendanceService attendanceService;

    private AttendanceRequest request;
    private AttendanceResponse response;

    @BeforeEach
    void setUp() {
        request = new AttendanceRequest();
        request.setEmployeeId(1L);
        request.setDate("2026-07-24");
        request.setCheckIn("09:00:00");
        request.setCheckOut("17:30:00");
        request.setAttendanceStatus(AttendanceStatus.PRESENT);

        response = AttendanceResponse.builder()
                .attendanceId(1L)
                .employeeId(1L)
                .employeeName("John Doe")
                .date("2026-07-24")
                .checkIn("09:00")
                .checkOut("17:30")
                .workingHours(8.5)
                .attendanceStatus(AttendanceStatus.PRESENT)
                .build();
    }

    @Test
    @DisplayName("POST /api/attendance - Success")
    void markAttendance_success() throws Exception {
        when(attendanceService.markAttendance(any(AttendanceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/attendance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendanceId").value(1L));
    }

    @Test
    @DisplayName("PUT /api/attendance/{id} - Success")
    void updateAttendance_success() throws Exception {
        when(attendanceService.updateAttendance(eq(1L), any(AttendanceRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/attendance/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendanceId").value(1L));
    }

    @Test
    @DisplayName("GET /api/attendance/{id} - Success")
    void getAttendanceById_success() throws Exception {
        when(attendanceService.getAttendanceById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/attendance/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.attendanceId").value(1L));
    }

    @Test
    @DisplayName("GET /api/attendance/employee/{employeeId} - Success")
    void getAttendanceByEmployee_success() throws Exception {
        when(attendanceService.getAttendanceByEmployee(1L)).thenReturn(List.of(response));

        mockMvc.perform(get("/api/attendance/employee/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].attendanceId").value(1L));
    }

    @Test
    @DisplayName("GET /api/attendance/date - Success")
    void getAttendanceByDate_success() throws Exception {
        when(attendanceService.getAttendanceByDate("2026-07-24")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/attendance/date").param("date", "2026-07-24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].attendanceId").value(1L));
    }

    @Test
    @DisplayName("GET /api/attendance/monthly/{employeeId} - Success")
    void getMonthlyAttendance_success() throws Exception {
        MonthlyAttendanceResponse monthlyResponse = MonthlyAttendanceResponse.builder()
                .employeeId(1L)
                .employeeName("John Doe")
                .period("July-2026")
                .statusSummary(Collections.singletonMap("PRESENT", 1L))
                .records(List.of(response))
                .build();

        when(attendanceService.getMonthlyAttendance(1L, "July-2026")).thenReturn(monthlyResponse);

        mockMvc.perform(get("/api/attendance/monthly/1").param("period", "July-2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.period").value("July-2026"))
                .andExpect(jsonPath("$.data.records[0].attendanceId").value(1L));
    }
}
