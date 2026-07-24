package com.srmcem.payroll.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.srmcem.payroll.dto.EmployeeRequest;
import com.srmcem.payroll.dto.EmployeeResponse;
import com.srmcem.payroll.enums.EmployeeStatus;
import com.srmcem.payroll.enums.Gender;
import com.srmcem.payroll.service.EmployeeService;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration/Slice tests for {@link EmployeeController} using MockMvc.
 */
@WebMvcTest(EmployeeController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private EmployeeService employeeService;

    private EmployeeRequest request;
    private EmployeeResponse response;

    @BeforeEach
    void setUp() {
        request = new EmployeeRequest();
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setGender(Gender.MALE);
        request.setDateOfBirth("1990-05-15");
        request.setEmail("john.doe@company.com");
        request.setPhone("+919876543210");
        request.setDepartmentId(1L);
        request.setDesignationId(1L);
        request.setJoiningDate("2023-01-10");
        request.setSalary(new BigDecimal("75000.00"));

        response = EmployeeResponse.builder()
                .employeeId(1L)
                .firstName("John")
                .lastName("Doe")
                .fullName("John Doe")
                .gender(Gender.MALE)
                .dateOfBirth("1990-05-15")
                .email("john.doe@company.com")
                .phone("+919876543210")
                .departmentId(1L)
                .departmentName("Engineering")
                .designationId(1L)
                .designationName("Software Engineer")
                .joiningDate("2023-01-10")
                .salary(new BigDecimal("75000.00"))
                .status(EmployeeStatus.ACTIVE)
                .build();
    }

    @Test
    @DisplayName("POST /api/employees - Success")
    void addEmployee_success() throws Exception {
        when(employeeService.addEmployee(any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(1L))
                .andExpect(jsonPath("$.data.fullName").value("John Doe"));
    }

    @Test
    @DisplayName("PUT /api/employees/{id} - Success")
    void updateEmployee_success() throws Exception {
        when(employeeService.updateEmployee(eq(1L), any(EmployeeRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/employees/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} - Success")
    void deleteEmployee_success() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Employee deleted successfully."));
    }

    @Test
    @DisplayName("GET /api/employees/{id} - Success")
    void getEmployeeById_success() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.employeeId").value(1L));
    }

    @Test
    @DisplayName("GET /api/employees - Success")
    void getAllEmployees_success() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].employeeId").value(1L));
    }

    @Test
    @DisplayName("GET /api/employees/search - Success")
    void searchEmployees_success() throws Exception {
        when(employeeService.searchEmployees("John")).thenReturn(List.of(response));

        mockMvc.perform(get("/api/employees/search").param("keyword", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].fullName").value("John Doe"));
    }
}
