package com.srmcem.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmcem.payroll.dto.DesignationRequest;
import com.srmcem.payroll.dto.DesignationResponse;
import com.srmcem.payroll.service.DesignationService;
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
 * Integration/Slice tests for {@link DesignationController} using MockMvc.
 */
@WebMvcTest(DesignationController.class)
@AutoConfigureMockMvc(addFilters = false)
class DesignationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DesignationService designationService;

    private DesignationRequest request;
    private DesignationResponse response;

    @BeforeEach
    void setUp() {
        request = new DesignationRequest();
        request.setDesignationName("Software Engineer");

        response = DesignationResponse.builder()
                .designationId(1L)
                .designationName("Software Engineer")
                .build();
    }

    @Test
    @DisplayName("POST /api/designations - Success")
    void addDesignation_success() throws Exception {
        when(designationService.addDesignation(any(DesignationRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/designations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.designationId").value(1L))
                .andExpect(jsonPath("$.data.designationName").value("Software Engineer"));
    }

    @Test
    @DisplayName("PUT /api/designations/{id} - Success")
    void updateDesignation_success() throws Exception {
        when(designationService.updateDesignation(eq(1L), any(DesignationRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/designations/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.designationId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/designations/{id} - Success")
    void deleteDesignation_success() throws Exception {
        doNothing().when(designationService).deleteDesignation(1L);

        mockMvc.perform(delete("/api/designations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Designation deleted successfully."));
    }

    @Test
    @DisplayName("GET /api/designations/{id} - Success")
    void getDesignationById_success() throws Exception {
        when(designationService.getDesignationById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/designations/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.designationId").value(1L));
    }

    @Test
    @DisplayName("GET /api/designations - Success")
    void getAllDesignations_success() throws Exception {
        when(designationService.getAllDesignations()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/designations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].designationId").value(1L));
    }
}
