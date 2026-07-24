package com.srmcem.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmcem.payroll.dto.DepartmentRequest;
import com.srmcem.payroll.dto.DepartmentResponse;
import com.srmcem.payroll.service.DepartmentService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration/Slice tests for {@link DepartmentController} using MockMvc.
 */
@WebMvcTest(DepartmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepartmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DepartmentService departmentService;

    private DepartmentRequest request;
    private DepartmentResponse response;

    @BeforeEach
    void setUp() {
        request = new DepartmentRequest();
        request.setDepartmentName("Engineering");
        request.setDescription("Software Engineering");

        response = DepartmentResponse.builder()
                .departmentId(1L)
                .departmentName("Engineering")
                .description("Software Engineering")
                .build();
    }

    @Test
    @DisplayName("POST /api/departments - Success")
    void addDepartment_success() throws Exception {
        when(departmentService.addDepartment(any(DepartmentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/departments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentId").value(1L))
                .andExpect(jsonPath("$.data.departmentName").value("Engineering"));
    }

    @Test
    @DisplayName("PUT /api/departments/{id} - Success")
    void updateDepartment_success() throws Exception {
        when(departmentService.updateDepartment(eq(1L), any(DepartmentRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/departments/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/departments/{id} - Success")
    void deleteDepartment_success() throws Exception {
        doNothing().when(departmentService).deleteDepartment(1L);

        mockMvc.perform(delete("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Department deleted successfully."));
    }

    @Test
    @DisplayName("GET /api/departments/{id} - Success")
    void getDepartmentById_success() throws Exception {
        when(departmentService.getDepartmentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/departments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.departmentId").value(1L));
    }

    @Test
    @DisplayName("GET /api/departments - Success")
    void getAllDepartments_success() throws Exception {
        when(departmentService.getAllDepartments()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].departmentId").value(1L));
    }
}
